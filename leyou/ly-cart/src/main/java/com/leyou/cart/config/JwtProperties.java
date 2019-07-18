package com.leyou.cart.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

/**
 * @package: com.leyou.cart.config
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {
    private String pubKeyPath;

    private PublicKey publicKey;

    private UserToken user = new UserToken();

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            log.info("【购物车】加载公钥成功");
        } catch (Exception e) {
            log.error("【购物车】加载公钥失败");
        }
    }

    @Data
    public class UserToken {
        private String cookieName;
    }

}
