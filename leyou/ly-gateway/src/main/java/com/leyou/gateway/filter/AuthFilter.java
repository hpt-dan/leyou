package com.leyou.gateway.filter;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @package: com.leyou.gateway.filter
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:对网关路由之前，进行拦截，判断用户是否登录了
 */
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private FilterProperties filterProperties;


    @Autowired
    private JwtProperties properties;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {

        //添加白名单
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String uri = request.getRequestURI();
        Set<String> allowPaths = filterProperties.getAllowPaths();
        for (String allowPath : allowPaths) {
            if(uri.startsWith(allowPath)){
                return false;
            }
        }
        return true;
    }




    @Override
    public Object run() throws ZuulException {


        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        try {
            //1:获取token,判断是否进行登录了。
            String token = CookieUtils.getCookieValue(request, properties.getUser().getCookieName());
            Payload<UserInfo> playload = JwtUtils.getInfoFromToken(token, properties.getPublicKey(), UserInfo.class);
            //2:解析token，判断是否在黑名单
            if (redisTemplate.hasKey(playload.getId())){
                throw new LyException(ExceptionEnum.LOGIN_ERROR_NOT_FOUND);
            }
        } catch (LyException e) {
            throw new LyException(ExceptionEnum.LOGIN_ERROR_NOT_FOUND);
        }


        return null;
    }
}
