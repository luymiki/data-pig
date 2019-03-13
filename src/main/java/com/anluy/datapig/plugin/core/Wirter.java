package com.anluy.datapig.plugin.core;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.utils.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 写入端插件接口
 *
 * @author hc.zeng
 * @create 2018-10-10 12:53
 */

public abstract class Wirter implements Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(Wirter.class);
    private volatile boolean shutdown = false;
    /**
     * 插件参数
     */
    private Map params;
    private RecordExchanger recordExchanger;
    private JobManager jobManager;
    /**
     * 格式化参数
     */
    private JSONObject formats;
    /**
     * 字段映射参数
     */
    private JSONObject mapping;
    /**
     * 格式化函数
     */
    private Map<String, Formatter> formatterMap = new HashMap<>();

    public Wirter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        this.params = params;
        this.recordExchanger = recordExchanger;
        this.jobManager = jobManager;
    }

    @Override
    public void execute() {
        try {
            formats = (JSONObject) params.get("format");
            mapping = (JSONObject) params.get("mapping");
            if(formats == null){
                formats = new JSONObject();
            }
            if(mapping == null){
                mapping = new JSONObject();
            }
            //初始化格式函数
            this.formatter();

            init(params);

            start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new DataPigException(e);
        } finally {
            end();
        }
    }

    @Override
    public void log(String jobId, Integer type, String name, String executor, String beanName,String params, Integer status, String msg, Integer times) {
        this.jobManager.getScheduleJobLogService().log(jobId, type, name, executor, beanName, params, status, msg, times);
    }

    @Override
    public synchronized void log(String msg) {
        ScheduleJobEntity job = jobManager.getScheduleJob();
        String msgStr = String.format("[线程%s\t流量%s]\t%s", Thread.currentThread().getName(),recordExchanger.getWriterMemorySizeString(), msg);
        this.log(job.getJobId(), job.getType(), job.getName(), job.getExecutor(), job.getBeanName(), job.getParams(), null, msgStr, null);
    }

    public Map getParams() {
        return params;
    }

    public RecordExchanger getRecordExchanger() {
        return recordExchanger;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public synchronized void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public JSONObject getFormats() {
        return formats;
    }

    public JSONObject getMapping() {
        return mapping;
    }

    public Map<String, Formatter> getFormatterMap() {
        return formatterMap;
    }

    /**
     * 任务执行器
     */
    public abstract class Task extends BaseTask {
        /**
         * 读取
         *
         * @return
         */
        protected void reader() {

        }

        /**
         * 写出
         */
        protected abstract void writer();
    }
}
