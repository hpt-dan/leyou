package com.leyou.gateway.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * @package: com.leyou.gateway.config
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties {

    private Set<String> allowPaths;
}
