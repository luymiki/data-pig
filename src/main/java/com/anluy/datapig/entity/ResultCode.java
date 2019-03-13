package com.anluy.datapig.entity;

/**
 * 错误码定义类
 */
public enum ResultCode {

    SUCCESS(200, "success"),

    /**
     * 参数校验失败 //参数{XX}校验失败
     **/
    DP_VALIDATION(25001, "参数校验失败"),

    DP_DATABASE_OPERATION(29999, "数据库操作失败"),
    NOT_FOUNDTION(26001, "查询不到值"),
    NOT_LOGIN(24000, "用户未登录"),
    ERROR(10001, "系统错误！");


    public final int status;
    public final String message;

    ResultCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
