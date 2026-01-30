package com.tradebeyond.backend.controller;

import com.tradebeyond.backend.dto.OrderDto;
import com.tradebeyond.backend.resp.Result;
import com.tradebeyond.backend.service.ApiService;
import com.tradebeyond.backend.vo.BaseResp;
import com.tradebeyond.backend.vo.OrdersVo;
import com.tradebeyond.backend.vo.ProductVo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class ApiController {
    @Autowired
    private ApiService apiService;


    /**
     * 依照 ProductID 取得商品
     *
     * @param productId 商品ID
     * @return 商品
     */
    @RequestMapping(value = "/Product/{product_id}", method = RequestMethod.GET)
    public Result<ProductVo> getProduct(@NonNull @PathVariable("product_id") Long productId) {
        log.info("Receive get product request, productId={}", productId);

        return Result.success(apiService.getProduct(productId));
    }


    /**
     * 建立訂單
     *
     * @param orderDto 訂單資訊
     * @return 是否成功
     */
    @RequestMapping(value = "/Order", method = RequestMethod.POST)
    public Result<BaseResp> createOrder(@RequestBody OrderDto orderDto) {
        log.info("Receive create order request, orderDto={}", orderDto);

        return Result.success(apiService.createOrder(orderDto.toBo()));
    }


    /**
     * 更新訂單
     *
     * @param orderId  訂單id
     * @param orderDto 更新內容
     * @return 是否成功
     */
    @RequestMapping(value = "/Order/{order_id}", method = RequestMethod.PATCH)
    public Result<BaseResp> updateOrder(@PathVariable("order_id") Long orderId, @RequestBody OrderDto orderDto) {
        log.info("Receive update order request, orderId={}", orderId);

        return Result.success(apiService.updateOrder(orderId, orderDto.toBo()));
    }


    /**
     * 刪除訂單
     *
     * @param orderId 訂單id
     * @return 是否成功
     */
    @RequestMapping(value = "/Order/{order_id}", method = RequestMethod.DELETE)
    public Result<BaseResp> deleteOrder(@NonNull @PathVariable("order_id") Long orderId) {
        log.info("Receive delete order request, orderId={}", orderId);

        return Result.success(apiService.deleteOrder(orderId));
    }


    /**
     * 刪除使用者與訂單
     *
     * @param userId 用戶id
     * @return 是否成功
     */
    @RequestMapping(value = "/User/{userId}", method = RequestMethod.DELETE)
    public Result<BaseResp> deleteUser(@NonNull @PathVariable("userId") Long userId) {
        log.info("Receive delete user request, userId={}", userId);

        return Result.success(apiService.deleteUserAndOrder(userId));
    }


    /**
     * 依使用者查詢訂單
     *
     * @param userId 用戶id
     * @return 用戶訂單
     */
    @RequestMapping(value = "/Order/{userId}", method = RequestMethod.GET)
    public Result<List<OrdersVo>> getUserOrders(@NonNull @PathVariable("userId") Long userId) {
        log.info("Receive get user orders, userId={}", userId);

        return Result.success(apiService.getUserOrders(userId));

    }

}
