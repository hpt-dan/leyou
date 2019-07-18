package com.leyou.user.client;

import com.leyou.user.dto.AddressDTO;
import com.leyou.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @package: com.leyou.user.client
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@FeignClient("user-service")
public interface UserClient {
    /**
     * 根据用户名和密码查询用户
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @GetMapping("query")
    UserDTO queryUserByUsernameAndPassword(@RequestParam("username") String username,
                                           @RequestParam("password") String password);

    /**
     * 根据
     * @param id 物流的id
     * @param id 地址id
     * @return 地址信息
     */
    @GetMapping("address")
    AddressDTO queryAddressById(@RequestParam("id") Long id);

}
