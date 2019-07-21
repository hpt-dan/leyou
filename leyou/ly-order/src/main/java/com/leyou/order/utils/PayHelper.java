package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfigImpl;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private WXPayConfigImpl payConfig;

    public String createOrder(Long orderId, Long totalPay, String desc){
        Map<String, String> data = new HashMap<>();
        // 商品描述
        data.put("body", desc);
        // 订单号
        data.put("out_trade_no", orderId.toString());
        //金额，单位是分
        data.put("total_fee", totalPay.toString());
        //调用微信支付的终端IP
        data.put("spbill_create_ip", "127.0.0.1");
        //回调地址
        data.put("notify_url", payConfig.getNotifyUrl());
        // 交易类型为扫码支付
        data.put("trade_type", payConfig.getTradeType());

        // 利用wxPay工具,完成下单
        Map<String, String> result = null;
        try {
            result = wxPay.unifiedOrder(data);
        } catch (Exception e) {
            log.error("【微信下单】创建预交易订单异常失败", e);
            throw new RuntimeException("微信下单失败", e);
        }
        // 校验业务状态
        checkResultCode(result);

        // 下单成功，获取支付链接
        String url = result.get("code_url");
        if (StringUtils.isBlank(url)) {
            throw new RuntimeException("微信下单失败，支付链接为空");
        }
        return url;
    }

    public void checkResultCode(Map<String, String> result) {
        // 检查业务状态
        String resultCode = result.get("result_code");
        if ("FAIL".equals(resultCode)) {
            log.error("【微信支付】微信支付业务失败，错误码：{}，原因：{}", result.get("err_code"), result.get("err_code_des"));
            throw new RuntimeException("【微信支付】微信支付业务失败");
        }
    }

    public void isValidSign(Map<String, String> result) throws Exception {
        boolean boo1 = WXPayUtil.isSignatureValid(result, payConfig.getKey(), WXPayConstants.SignType.MD5);
        boolean boo2 = WXPayUtil.isSignatureValid(result, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
        if (!boo1 && !boo2) {
            throw new RuntimeException("【微信支付回调】签名有误");
        }
    }

}