package com.leyou.common.enums;


import lombok.Getter;

@Getter
public enum ExceptionEnum {
    //修改DATA_TRANSFER_ERROR；
    PRICE_CANNOT_BE_NULL(400, "价格不能为空！"),
    DATA_TRANSFER_ERROR(400,"数据转换异常"),
    CATEGORY_NOT_FOND(400,"没有查询到数据。"),
    BRAND_NOT_FOUND(204, "没有查询到数据"),
    INSERT_OPERATION_FAIL(500,"添加失败"),
    INVALID_FILE_TYPE(500, "上传失败，请稍后再试"),
    UPDATE_OPERATION_FAIL(500,"上传失败"),
    CATEGORY_NOT_FOUND(500,"品牌信息错误"),
    DELETE_OPERATION_FAIL(500,"删除失败")
    ;
   
    private int status;
    private String message;

    ExceptionEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}