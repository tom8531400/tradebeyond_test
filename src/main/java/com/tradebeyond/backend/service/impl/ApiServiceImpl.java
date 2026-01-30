package com.tradebeyond.backend.service.impl;

import com.tradebeyond.backend.bo.OrderBo;
import com.tradebeyond.backend.bo.ProductBo;
import com.tradebeyond.backend.bo.UsersBo;
import com.tradebeyond.backend.enums.ProductCategoryEnum;
import com.tradebeyond.backend.enums.StatusCode;
import com.tradebeyond.backend.exception.BusinessException;
import com.tradebeyond.backend.factory.product.ProductFactory;
import com.tradebeyond.backend.mapper.OrdersDao;
import com.tradebeyond.backend.mapper.ProductDao;
import com.tradebeyond.backend.mapper.UsersDao;
import com.tradebeyond.backend.service.ApiService;
import com.tradebeyond.backend.vo.BaseResp;
import com.tradebeyond.backend.vo.OrdersVo;
import com.tradebeyond.backend.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private TransactionTemplate transactionTemplate;
    @Autowired
    private Map<String, ProductFactory> productFactoryMap;

    @Override
    public ProductVo getProduct(Long productId) {

        return checkProduct(productId).toVo();
    }

    @Override
    public BaseResp createOrder(OrderBo orderBo) {
        Long userId = orderBo.getUserId();
        Long productId = orderBo.getProductId();
        Integer orderAmount = orderBo.getOrderAmount();

        if (userId == null || productId == null || orderAmount == null || orderAmount <= 0) {
            log.error("userId or productId is null or orderAmount is null");
            throw new BusinessException(StatusCode.PARAMS_INVALID.getCode());
        }
        checkUser(userId);
        checkProduct(productId);

        int insert = ordersDao.insert(orderBo);
        if (insert != 1) {
            log.error("Create order failed, insert count={}, order={}", insert, orderBo);
            throw new BusinessException(StatusCode.ORDER_CREATE_FAILED.getCode());
        }
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
    public List<OrdersVo> getUserOrders(Long userId) {
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
