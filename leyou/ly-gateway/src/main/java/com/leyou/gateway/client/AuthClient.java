package com.leyou.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient("auth-service")
public interface AuthClient {
    /**
     * 微服务认证并申请令牌
     * @param id 服务id
     * @param secret 密码
     * @return 无
     */
    @GetMapping("authorization")
    String authorize(@RequestParam("id") Long id, @RequestParam("secret") String secret);
}