package com.leyou.auth.entity;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Data
@Table(name = "tb_application")
public class ApplicationInfo {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务密钥
     */
    private String secret;
    /**
     * 服务信息
     */
    private String info;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}