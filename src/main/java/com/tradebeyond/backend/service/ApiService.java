package com.tradebeyond.backend.service;

import com.tradebeyond.backend.bo.OrderBo;
import com.tradebeyond.backend.vo.BaseResp;
import com.tradebeyond.backend.vo.OrdersVo;
import com.tradebeyond.backend.vo.ProductVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ApiService {

    ProductVo getProduct(Long productId);

    BaseResp createOrder(OrderBo orderBo);

    BaseResp updateOrder(Long orderId, OrderBo orderBo);

    BaseResp deleteOrder(Long orderId);

    BaseResp deleteUserAndOrder(Long userId);

    List<OrdersVo> getUserOrders(Long userId, HttpServletRequest request);
}
