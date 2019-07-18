package com.leyou.user.config;

import com.leyou.user.interceptors.PrivilegeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @package: com.leyou.user.config
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private PrivilegeInterceptor privilegeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有但是除了swagger
        registry.addInterceptor(privilegeInterceptor).excludePathPatterns("/swagger-ui.html");
    }
}
