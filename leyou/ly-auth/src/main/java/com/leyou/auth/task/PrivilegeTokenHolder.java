package com.leyou.auth.task;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @package: com.leyou.auth.task
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Component
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeTokenHolder {

    @Autowired
    private JwtProperties props;

    @Autowired
    private AuthService authService;

    /**
     * token刷新间隔  24H
     */
    private static final long TOKEN_REFRESH_INTERVAL = 86400000L;

    /**
     * token获取失败后重试的间隔
     */
    private static final long TOKEN_RETRY_INTERVAL = 10000L;

    private String token;

    //每隔24H执行一次，并且启动会自动执行一次，第一次执行是当前启动后
    @Scheduled(fixedDelay = TOKEN_REFRESH_INTERVAL)
    public void loadToken() throws InterruptedException {

        while (true){
            try {

                // 向ly-auth发起请求，获取JWT

                //自己调用自己的service，生成token

                this.token =  authService.authenticate(props.getApp().getId(),props.getApp().getSecret());
                log.info("【授权中心】定时获取token成功");
                break;
            } catch (Exception e) {
                log.info("【授权中心】定时获取token失败");
            }
            // 休眠10秒，再次重试
            Thread.sleep(TOKEN_RETRY_INTERVAL);
        }

    }

    public String getToken(){
        return token;
    }
}
