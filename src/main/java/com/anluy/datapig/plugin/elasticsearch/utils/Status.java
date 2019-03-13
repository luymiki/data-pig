package com.anluy.datapig.plugin.elasticsearch.utils;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-26 14:44
 */
public class Status {
    /**
     * 成功标记
     */
    public static final String STATUS_SUCCESS = "success";
    /**
     * 失败标记
     */
    public static final String STATUS_ERROR = "error";

    private String status;
    private String message;
    /**
     * 上下文信息
     */
    private Map<String, Object> context;
    /**
     * 状态码 eg: 200 OK
     */
    private Integer code;

    public Status(String status) {
        this.status = status;
    }

    public Status(String status, Integer code) {
        this.status = status;
        this.code = code;
    }

    public Status(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status(String status, String message, Integer code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getContext() {
        if (context == null) {
            context = new HashMap<String, Object>();
        }
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public void setError(String message) {
        this.setStatus(STATUS_ERROR);
        if (message != null) {
            this.setMessage(message);
        }
    }

    public void setSuccess(String message) {
        this.setStatus(STATUS_SUCCESS);
        if (message != null) {
            this.setMessage(message);
        }
    }

    /**
     * 是否失败
     *
     * @return
     */
    public boolean isFailed() {
        return STATUS_ERROR.equals(this.getStatus());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static Status ok() {
        return new Status(STATUS_SUCCESS, HttpStatus.SC_OK);
    }


    public static Status error() {
        return new Status(STATUS_ERROR, HttpStatus.SC_OK);//200表示请求是成功返回的，但是服务端方法执行报错
    }
}