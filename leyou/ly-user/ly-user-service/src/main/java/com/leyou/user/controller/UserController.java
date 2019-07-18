package com.leyou.user.controller;

import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @package: com.leyou.user.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 验证用户名或手机号是否唯一
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data")String data,
                                                 @PathVariable("type")Integer type){

        return ResponseEntity.ok(userService.checkUserData(data, type));
    }


    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){

        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code) {
        userService.register(user,code);
        return ResponseEntity.ok().build();
    }



    /**
     * 根据用户名和密码查询用户
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @GetMapping("query")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(@RequestParam("username") String username,
                                                                  @RequestParam("password") String password){
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username, password));
    }
}
