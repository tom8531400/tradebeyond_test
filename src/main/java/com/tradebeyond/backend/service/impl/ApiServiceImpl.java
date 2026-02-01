package com.tradebeyond.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebeyond.backend.bo.*;
import com.tradebeyond.backend.enums.*;
import com.tradebeyond.backend.exception.BusinessException;
import com.tradebeyond.backend.factory.product.ProductFactory;
import com.tradebeyond.backend.mapper.OrdersDao;
import com.tradebeyond.backend.mapper.OutboxEventDao;
import com.tradebeyond.backend.mapper.ProductDao;
import com.tradebeyond.backend.mapper.UsersDao;
import com.tradebeyond.backend.redis.RedisUtils;
import com.tradebeyond.backend.service.ApiService;
import com.tradebeyond.backend.vo.BaseResp;
import com.tradebeyond.backend.vo.OrdersVo;
import com.tradebeyond.backend.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApiServiceImpl implements ApiService {
    @Autowired
    private ProductDao productDao;
    @Autowired
    private UsersDao usersDao;
    @Autowired
    private OrdersDao ordersDao;
    @Autowired
    private OutboxEventDao outboxEventDao;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private Map<String, ProductFactory> productFactoryMap;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisUtils redisUtils;


    private static final String LUA_CACHE_USER_KEY =
            "local count = redis.call('INCR', KEYS[1]) " +
                    "if count == 1 then redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1])) end " +
                    "if count > tonumber(ARGV[2]) then return 0 " +
                    "else return 1 end";
    private static final DefaultRedisScript<Long> CACHE_USER_SCRIPT_LUA;
    static{
        CACHE_USER_SCRIPT_LUA = new DefaultRedisScript<>();
        CACHE_USER_SCRIPT_LUA.setScriptText(LUA_CACHE_USER_KEY);
        CACHE_USER_SCRIPT_LUA.setResultType(Long.class);
        CACHE_USER_SCRIPT_LUA.afterPropertiesSet();
    }


    @Override
    public ProductVo getProduct(Long productId) {

        return checkProduct(productId).toVo();
    }

    @Override
    public BaseResp createOrder(OrderBo orderBo) {
        Long userId = orderBo.getUserId();
        Long productId = orderBo.getProductId();
        Integer orderAmount = orderBo.getOrderAmount();

        // 校驗參數
        if (userId == null || productId == null || orderAmount == null || orderAmount <= 0) {
            log.error("userId or productId is null or orderAmount is null");
            throw new BusinessException(StatusCode.PARAMS_INVALID.getCode());
        }
        checkUser(userId);
        checkProduct(productId);

        // 處理事務
        transactionTemplate.executeWithoutResult(status -> {
            // 存入主訂單
            int insert = ordersDao.insert(orderBo);
            if (insert != 1) {
                log.error("Create order failed");
                throw new BusinessException(StatusCode.ORDER_CREATE_FAILED.getCode());
            }
            // 構建outbox
            OutboxEventBo bo = new OutboxEventBo();
            String uuid = UUID.randomUUID().toString();

            bo.setEventId(uuid);
            bo.setOrderId(orderBo.getOrderId());
            bo.setUserId(userId);
            bo.setRetryCount(RetryEnum.NONE.getCount()); // 初始次數 = 0
            bo.setEventType(EventTypeEnum.ORDER_CREATED.getValue()); // 事件 = 訂單建立
            bo.setStatus(OutBoxStatusEnum.NEW.getCode()); // 狀態 = 待發送

            Payload payload = new Payload();
            payload.setOrderId(orderBo.getOrderId());
            payload.setUserId(userId);
            payload.setType(PayloadEnum.ORDER_CREATED.getCode());
            String message;
            try {
                message = objectMapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new BusinessException(StatusCode.FAIL.getCode());
            }
            bo.setMessage(message);
            Date date = new Date();
            bo.setNextRetryAt(date);
            bo.setCreateTime(date);
            bo.setUpdateTime(date);

            // 存入outbox
            int outbox = outboxEventDao.insert(bo);
            if (outbox != 1) {
                log.error("Create outbox failed");
                throw new BusinessException(StatusCode.ORDER_OUTBOX_CREATE_FAILED.getCode());
            }
        });
        return BaseResp.success();
    }

    @Override
    public BaseResp updateOrder(Long orderId, OrderBo orderBo) {
        checkOrder(orderId);

        orderBo.setOrderId(orderId);
        int updated = ordersDao.updateByPrimaryKeySelective(orderBo);
        if (updated != 1) {
            log.error("Update order failed, update count={}, order={}", updated, orderBo);
            throw new BusinessException(StatusCode.ORDER_UPDATE_FAILED.getCode());
        }
        return BaseResp.success();
    }

    @Override
    public BaseResp deleteOrder(Long orderId) {
        checkOrder(orderId);

        ordersDao.deleteByPrimaryKey(orderId);
        return BaseResp.success();
    }

    @Override
    public BaseResp deleteUserAndOrder(Long userId) {
        checkUser(userId);

        transactionTemplate.executeWithoutResult(status -> {
            List<Long> longs = ordersDao.selectUserOrderIds(userId);

            if (longs != null && !longs.isEmpty()) {
                int delOrderRows = ordersDao.deleteByOrderIds(longs);
                log.info("delete order count={}, deleted={} for userId={}", longs.size(), delOrderRows, userId);
            }
            int delUserRows = usersDao.deleteByPrimaryKey(userId);
            if (delUserRows != 1) {
                log.error("delete user failed, delete count={}, user={}", delUserRows, userId);
                throw new BusinessException(StatusCode.USER_DELETE_FAILED.getCode());
            }
        });
        return BaseResp.success();
    }


    @Override
    public List<OrdersVo> getUserOrders(Long userId, HttpServletRequest request) {
        // 限流每個用戶一小時不能超過500次訪問
        String path = request.getRequestURI();

        final String cacheUserKey = RedisEnum.CACHE_USER_KEY.getKey() + ":" + path + ":" + userId;
        Long aLong = redisUtils.cacheUserCount(CACHE_USER_SCRIPT_LUA, cacheUserKey, 3600, 500);
        if(aLong != null && aLong == 0) {
            log.warn("The user:{} has exceeded 500 requests in one hour", userId);
            throw new BusinessException(StatusCode.FAIL.getCode());
        }

        checkUser(userId);

        List<OrdersVo> resp = new ArrayList<>();

        List<OrderBo> orderBos = ordersDao.selectByUserId(userId);

        if (orderBos != null && !orderBos.isEmpty()) {
            Set<Long> productIds = orderBos.stream().map(OrderBo::getProductId).collect(Collectors.toSet());
            List<ProductBo> productBos = productDao.selectAllByProductId(productIds);
            if (productBos == null || productBos.isEmpty()) {
                log.error("productBos is empty");
                throw new BusinessException(StatusCode.PRODUCT_NOT_FOUND.getCode());
            }
            Map<Long, ProductBo> productBoMap = productBos.stream().collect(Collectors.toMap(ProductBo::getProductId, Function.identity()));

            for (OrderBo orderBo : orderBos) {
                Integer orderAmount = orderBo.getOrderAmount();
                ProductBo productBo = productBoMap.get(orderBo.getProductId());
                Long productCategoryId = productBo.getProductCategoryId();
                BigDecimal totalCost = productFactoryMap.get(ProductCategoryEnum.getCategoryByCode(productCategoryId)).calculateTotalCost(productBo, orderBo);

                OrdersVo ordersVo = new OrdersVo();
                ordersVo.setOrderId(orderBo.getOrderId());
                ordersVo.setUserId(orderBo.getUserId());
                ordersVo.setProductId(orderBo.getProductId());
                ordersVo.setOrderAmount(orderAmount);
                ordersVo.setOrderPrice(totalCost);
                resp.add(ordersVo);
            }
        }
        return resp;
    }

    // 檢核user參數
    private UsersBo checkUser(Long userId) {
        UsersBo usersBo = usersDao.selectByPrimaryKey(userId);
        if (usersBo == null) {
            log.warn("user not exist, userId={}", userId);
            throw new BusinessException(StatusCode.USER_NOT_FOUND.getCode());
        }
        return usersBo;
    }

    // 檢核product參數
    private ProductBo checkProduct(Long productId) {
        ProductBo productBo = productDao.selectByPrimaryKey(productId);
        if (productBo == null) {
            log.warn("product not exist, productId={}", productId);
            throw new BusinessException(StatusCode.PRODUCT_NOT_FOUND.getCode());
        }
        return productBo;
    }

    // 檢核order參數
    private OrderBo checkOrder(Long orderId) {
        OrderBo orders = ordersDao.selectByPrimaryKey(orderId);
        if (orders == null) {
            log.warn("order not exist, orderId={}", orderId);
            throw new BusinessException(StatusCode.ORDER_NOT_FOUND.getCode());
        }
        return orders;
    }
}
