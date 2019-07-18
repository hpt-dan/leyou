package com.leyou.gateway.filter;

import com.leyou.gateway.config.JwtProperties;
import com.leyou.gateway.task.PrivilegeTokenHolder;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

/**
 * @package: com.leyou.gateway.filter
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeFilter extends ZuulFilter {

    @Autowired
    private PrivilegeTokenHolder tokenHolder;

    @Autowired
    private JwtProperties props;


    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * PRE_DECORATION_FILTER 是Zuul默认的处理请求头的过滤器，我们放到这个之后执行
     *
     * @return 顺序
     */
    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //需要把token加入到请求头中

        RequestContext currentContext = RequestContext.getCurrentContext();
        //添加请求头，key：value格式
        String token = tokenHolder.getToken();
        String headerName = props.getApp().getHeaderName();
        currentContext.addZuulRequestHeader(headerName,token);
        return null;
    }
}
