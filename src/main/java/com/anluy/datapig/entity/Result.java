package com.anluy.datapig.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.MessageFormat;

/**
 * 公共后端处理状态返回实体
 *
 * @author hc.zeng 2017-05-04
 */
public class Result {
    /**
     * 成功标记
     */
    public static final String SUCCESS = "success";
    public static final int OK = 0;
    private int status;//状态码
    private String message;//状态信息
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String request;//请求地址
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object context;//错误信息
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;//数据信息


    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * 实例化成功的返回结果实体
     *
     * @param resultCode
     * @param data
     */
    public Result(ResultCode resultCode, Object data) {
        this.status = resultCode.status;
        this.message = resultCode.message;
        this.data = data;
    }

    /**
     * 实例化失败的返回结果实体
     *
     * @param resultCode 错误状态码
     * @param context    错误内容
     * @param request    uri地址
     */
    public Result(ResultCode resultCode, Object context, String request) {
        this.status = resultCode.status;
        this.message = resultCode.message;
        this.request = request;
        this.context = context;
    }

    /**
     * 实例化成功的返回结果实体
     *
     * @param data
     */
    public static Result success(Object data) {
        return new Result(ResultCode.SUCCESS, data);
    }

    /**
     * 实例化失败的返回结果实体
     *
     * @param resultCode 错误状态码
     * @param context    错误内容
     */
    public static Result error(ResultCode resultCode, Object context) {
        return new Result(resultCode, context, ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI());
    }
    /**
     * 实例化失败的返回结果实体
     *
     * @param resultCode 错误状态码
     */
    public static Result error(ResultCode resultCode) {
        return new Result(resultCode, resultCode.message, ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI());
    }
    /**
     * 实例化失败的返回结果实体
     *
     * @param code 错误状态码
     */
    public static Result error(int code, String message) {
        return new Result(code,message);
    }


    public int getStatus() {
        return status;
    }

    public Result setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getRequest() {
        return request;
    }

    public Result setRequest(String request) {
        this.request = request;
        return this;
    }

    public Object getContext() {
        return context;
    }

    public Result setContext(Object context) {
        this.context = context;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }

    public Result format(String... format){
        if (!ArrayUtils.isEmpty(format) && !StringUtils.isEmpty(this.message)) {
            this.message = MessageFormat.format(this.message, format);
        }
        return this;
    }

}
