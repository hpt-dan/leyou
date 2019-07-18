package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @package: com.leyou.auth.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@RestController
public class AuthController {


    @Autowired
    private AuthService authService;


    /**
     * 登录授权
     * @param username 用户名
     * @param password 密码
     * @return 无
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response) {
        // 登录
        authService.login(username, password, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 验证用户信息
     *
     * @return 用户信息
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(HttpServletRequest request, HttpServletResponse response) {
        // 成功后直接返回
        // 用来每次点击刷新token
        return ResponseEntity.ok(authService.verifyUser(request, response));
    }

    /**
     * 退出登录
     */
    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        authService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 微服务认证并申请令牌
     *
     * @param id 服务id
     * @param secret 密码
     * @return 无
     */
    @GetMapping("authorization")
    public ResponseEntity<String> authorize(@RequestParam("id") Long id, @RequestParam("secret") String secret) {
        return ResponseEntity.ok(authService.authenticate(id, secret));
    }



}
