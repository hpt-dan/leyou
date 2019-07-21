package com.leyou.order.controller;


import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @package: com.leyou.order.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 生成订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody @Valid OrderDTO orderDTO){

        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }


    /**
     * 根据订单id查询订单
     * @param orderId
     * @return
     */
    @GetMapping("{orderId}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("orderId")Long orderId){
        return ResponseEntity.ok(orderService.queryOrderById(orderId));
    }

    /**
     *创建二维码的URL
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> getPayUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }




}