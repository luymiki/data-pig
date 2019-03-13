package com.anluy.datapig.plugin.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-11-01 15:24
 */

public class Executor {
    private static int corePoolSize = 15;
    private static int maximumPoolSize = 100;
    public static ExecutorService executorService;
    static {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("DataPig-pool-%d").build();
        executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024),threadFactory);
    }
}
