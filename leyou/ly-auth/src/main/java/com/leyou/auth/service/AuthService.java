package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.ApplicationInfo;
import com.leyou.auth.mapper.ApplicationInfoMapper;
import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

/**
 * @package: com.leyou.auth.service
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties prop;

    public void login(String username, String password, HttpServletResponse response) {

        try {
            UserDTO userDTO = userClient.queryUserByUsernameAndPassword(username, password);
            UserInfo userInfo = BeanHelper.copyProperties(userDTO, UserInfo.class);
            userInfo.setRole("admin");
            String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());
            CookieUtils.newBuilder()
                    .response(response)
                    .name(prop.getUser().getCookieName())
                    .value(token)
                    .domain(prop.getUser().getCookieDomain())
                    .maxAge(prop.getUser().getExpire())
                    .httpOnly(true)
                    .build();
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }

    /**
     * 验证用户是否登录
     * @param request
     * @param response
     * @return
     */
    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {

        try {
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());

            //验证token是否添加到黑名单那中
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            String id = payload.getId();
            if (redisTemplate.hasKey(id)) {
                // 抛出异常，证明token无效，直接返回401
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }
            UserInfo info = payload.getInfo();

            //重新生成token，和cookie
            String newToken = JwtUtils.generateTokenExpireInMinutes(info, prop.getPrivateKey(), prop.getUser().getExpire());

            //把cookie写入response
            CookieUtils.newBuilder()
                    .response(response)
                    .name(prop.getUser().getCookieName())
                    .value(newToken)
                    .domain(prop.getUser().getCookieDomain())
                    .path("/")
                    .maxAge(prop.getUser().getExpire()*60)
                    .build();


            UserInfo userInfo = payload.getInfo();
            return userInfo;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }


    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 用户退出登录
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        try {
            String cookieName = prop.getUser().getCookieName();
            String token = CookieUtils.getCookieValue(request, cookieName);
            PublicKey publicKey = prop.getPublicKey();
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);

            long time = payload.getExpiration().getTime() - System.currentTimeMillis();

            if (time > 5000) {
                redisTemplate.opsForValue().set(payload.getId(), "", time, TimeUnit.MILLISECONDS);
            }

            //新生成cookie，内容为双引号，有效周期为0

            Cookie cookie = new Cookie(prop.getUser().getCookieName(), "");
            cookie.setDomain(prop.getUser().getCookieDomain());
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.OUT_LOGIN_ERROR);
        }
    }


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationInfoMapper infoMapper;


    /**
     * 根据各个服务的id和service-name来生成token
     * @param id
     * @param secret
     * @return
     */
    public String authenticate(Long id, String secret) {
        try {
            //校验id和secret
            //根据服务的id。查询服务信息
            ApplicationInfo app = infoMapper.selectByPrimaryKey(id);

            // 判断是否为空
            if (app == null) {
                // id不存在，抛出异常
                throw new LyException(ExceptionEnum.INVALID_SERVER_ID_SECRET);
            }
            // 校验密码,if为验密没有通过
            if (!passwordEncoder.matches(secret, app.getSecret())) {
                // 密码错误
                throw new LyException(ExceptionEnum.INVALID_SERVER_ID_SECRET);
            }

            //生成token,info

            //封装appInfo
            AppInfo appInfo = new AppInfo();
            appInfo.setId(id);
            appInfo.setServiceName(app.getServiceName());

            appInfo.setTargetList(infoMapper.queryTargetIdList(id));

            String token = JwtUtils.generateTokenExpireInMinutes(appInfo, prop.getPrivateKey(), prop.getApp().getExpire());

            return token;
        } catch (LyException e) {
            //授权失败
            throw new LyException(ExceptionEnum.INVALID_SERVER_ID_SECRET);
        }

    }


}
