package com.leyou.order.interceptor;

import com.leyou.order.config.JwtProperties;
import com.leyou.order.task.PrivilegeTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * feign进行服务间的调用拦截
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeInterceptor implements RequestInterceptor {

    @Autowired
    private JwtProperties props;

    @Autowired
    private PrivilegeTokenHolder tokenHolder;

    @Override
    public void apply(RequestTemplate template) {
        //把token加入到feign的请求头中
        template.header(props.getApp().getHeaderName(),tokenHolder.getToken());
    }
}