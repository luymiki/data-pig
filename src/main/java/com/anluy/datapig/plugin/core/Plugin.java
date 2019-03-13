package com.anluy.datapig.plugin.core;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 插件接口
 *
 * @author hc.zeng
 * @create 2018-10-10 14:28
 */
public interface Plugin  extends PluginLogger, Callable<Boolean>{
    String PARAMS_KEY = "PARAMS_KEY";

    /**
     * 对外统一执行入口
     */
    void execute();

    /**
     * 初始化
     * @param params
     * @return
     */
    Object init(Map params);

    /**
     * 开始执行
     * @return
     */
    Object start();

    /**
     * 停止执行
     * @return
     */
    Object shutdown();

    /**
     * 执行完成
     * @return
     */
    Object end();

    /**
     * 定义格式化函数
     */
    void formatter();
}
