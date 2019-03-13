package com.anluy.datapig.job.task;

import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-11-05 17:02
 */

public abstract class DataPigTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPigTask.class);
    private volatile boolean shutdown = false;
    private RecordExchanger recordExchanger;
    private JobManager jobManager;
    /**
     * 参数
     */
    private Map params;

    public DataPigTask() {
    }

    public DataPigTask(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        this.recordExchanger = recordExchanger;
        this.jobManager = jobManager;
        this.params = params;
    }

    /**
     * 任务入口
     */
    public void execute(){
        try {
            task();
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
            throw new DataPigException(e);
        }finally {
            shutdown();
        }
    }

    /**
     * 任务入口
     */
    public abstract void task();

    public abstract void shutdown();

    public RecordExchanger getRecordExchanger() {
        return recordExchanger;
    }

    public void setRecordExchanger(RecordExchanger recordExchanger) {
        this.recordExchanger = recordExchanger;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public Map getParams() {
        return params;
    }

    public void setParams(Map params) {
        this.params = params;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public synchronized void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
