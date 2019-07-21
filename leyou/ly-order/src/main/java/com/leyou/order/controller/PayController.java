package com.leyou.order.controller;


import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("pay")
public class PayController {


    @Autowired
    private OrderService orderService;

    /**
     * 微信支付成功回调
     * @param result
     * @return
     */
    @PostMapping(value = "/wx/notify", produces = "application/xml")
    public Map<String, String> hello(@RequestBody Map<String,String> result){
        // 处理回调
        log.info("[支付回调] 接收微信支付回调, 结果:{}", result);
        orderService.handleNotify(result);

        // 返回成功
        Map<String, String> msg = new HashMap<>();
        msg.put("return_code", "SUCCESS");
        msg.put("return_msg", "OK");
        return msg;
    }

    /**
     * 查询用户支付状态
     * @param orderId
     * @return
     */
    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryPayStatus(orderId));
    }
}