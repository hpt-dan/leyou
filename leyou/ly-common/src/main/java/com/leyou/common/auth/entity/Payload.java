package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.Date;


@Data
public class Payload<T> {
    private String id;
    private T info;
    private Date expiration;
}