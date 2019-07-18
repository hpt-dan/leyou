package com.leyou.user.interceptors;

import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @package: com.leyou.user.interceptors
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class PrivilegeInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties props;

    //能请求返回true，不能请求返回false
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {

            //获取token，完事后，解析token，成功，校验服务的合法性，一切正常放行
            //从请求头中获取token
            String token = request.getHeader(props.getApp().getHeaderName());

            //解析token
            Payload<AppInfo> appInfoPayload = JwtUtils.getInfoFromToken(token, props.getPublicKey(), AppInfo.class);

            //从载荷中获取封装的AppInfo
            AppInfo info = appInfoPayload.getInfo();

            //获取原始请求的目标服务列表
            List<Long> targetList = info.getTargetList();

            //如果当前服务id在列表中则放行，否则拦截（false）
            if (targetList.contains(props.getApp().getId())){
                return true;
            }
        } catch (Exception e) {
            log.error("服务访问被拒绝,token认证失败!", e);
            throw new LyException(ExceptionEnum.INVALID_TOKEN);
        }
        return false;
    }


}
