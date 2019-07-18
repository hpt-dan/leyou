package com.leyou.user.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

@ConfigurationProperties(prefix = "ly.jwt")
@Data
@Slf4j
public class JwtProperties implements InitializingBean {

    private String pubKeyPath;

    private PublicKey publicKey;

    private AppTokenInfo app = new AppTokenInfo();

    @Data
    public class AppTokenInfo {

        private Long id;
        private String secret;
        private String headerName;
    }

    //这个方法在属性赋值完之后才运行，运行的目的是拿公私钥加载的路径去加载，公钥和私钥对象
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);

        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}