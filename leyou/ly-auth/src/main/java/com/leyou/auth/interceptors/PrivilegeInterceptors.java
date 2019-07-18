package com.leyou.auth.interceptors;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.task.PrivilegeTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @package: com.leyou.auth.interceptors
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:利用Feign的拦截器，来对请求头处理
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeInterceptors implements RequestInterceptor {

    @Autowired
    private JwtProperties props;

    @Autowired
    private PrivilegeTokenHolder tokenHolder;


    @Override
    public void apply(RequestTemplate template) {
        // 获取token
        String token = tokenHolder.getToken();
        // 给请求添加头信息
        template.header(props.getApp().getHeaderName(), token);
    }

}
