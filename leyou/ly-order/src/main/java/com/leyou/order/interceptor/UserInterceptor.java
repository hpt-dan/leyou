package com.leyou.order.interceptor;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @package: com.leyou.cart.interceptors
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:登录状态下的购物车业务
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties prop;

    private static ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            //获取token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            tl.set(payload.getInfo());
            log.info("【订单解析用户：】用户信息解析成功");
            return true;
        } catch (Exception e) {
            log.error("【订单解析用户：】用户信息解析失败，未登录");
            return false;
        }
    }



    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        tl.remove();
    }


    /**
     * 获取useInf信息
     * @return
     */
    public static UserInfo getUserInfo(){
        return tl.get();
    }
}
