package com.leyou.common.enums;


import lombok.Getter;

@Getter
public enum ExceptionEnum {
    //修改DATA_TRANSFER_ERROR；
    PRICE_CANNOT_BE_NULL(400, "价格不能为空！"),
    DATA_TRANSFER_ERROR(400,"数据转换异常"),
    CATEGORY_NOT_FOND(400,"没有查询到数据。")

    ;
   
    private int status;
    private String message;

    ExceptionEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}