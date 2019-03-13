package com.anluy.datapig.plugin.core;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-20 17:02
 */

public interface PluginLogger {
    /**
     * 日志输出
     * @param msg
     * @return
     */
    void log(String msg);

    /**
     * 日志输出
     * @param jobId
     * @param type
     * @param name
     * @param executor
     * @param beanName
     * @param params
     * @param status
     * @param msg
     * @param times
     */
    void log(String jobId, Integer type, String name, String executor, String beanName, String params, Integer status, String msg, Integer times);
}
