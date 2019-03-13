package com.anluy.datapig.plugin.core;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.utils.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取端插件接口
 *
 * @author hc.zeng
 * @create 2018-10-10 12:53
 */

public abstract class Reader implements Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reader.class);
    private volatile boolean shutdown = false;
    private RecordExchanger recordExchanger;
    /**
     * 插件参数
     */
    private Map params;
    private JobManager jobManager;
    /**
     * 是否是增量运行
     */
    private boolean increment = false;
    private boolean incrementAll = false;

    /**
     * 增量运行的时间字段
     */
    private String incrementColumn;

    private java.util.Date incrementTime;

    /**
     * 最后一条记录的时间
     */
    private java.util.Date lastDataTime;

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

    public Reader(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
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

            Integer increment = jobManager.getScheduleJob().getIncrement();
            Integer incrementAll = jobManager.getScheduleJob().getIncrementAll();
            String incrementColumn = jobManager.getScheduleJob().getIncrementColumn();
            java.util.Date incrementTime = jobManager.getScheduleJob().getIncrementTime();

            /**
             * 增量运行
             */
            if (increment != null && increment == 1) {
                this.increment = true;
                //如果全量运行一次，增量运行标记设置为false
                if (incrementAll != null && incrementAll == 1) {
                    this.increment = false;
                    this.incrementAll = true;
                    this.incrementColumn = incrementColumn;
                } else {
                    if (StringUtils.isBlank(incrementColumn)) {
                        throw new DataPigException("增量运行的时间字段不能为空!");
                    }
                    if (incrementTime == null) {
                        throw new DataPigException("增量运行的时间值不能为空!");
                    }
                    this.incrementColumn = incrementColumn;
                    this.incrementTime = incrementTime;
                }
            } else {
                this.increment = false;
            }

            start();
        } catch (Exception e) {
            recordExchanger.terminate();
            LOGGER.error(e.getMessage(), e);
            throw new DataPigException(e);
        } finally {
            if (incrementAll || (increment && lastDataTime != null)) {
                ScheduleJobEntity entity = new ScheduleJobEntity();
                entity.setJobId(jobManager.getScheduleJob().getJobId());
                entity.setIncrementTime(lastDataTime);
                entity.setIncrementAll(0);
                getJobManager().getScheduleJobService().update(entity);
            }
            end();
        }
    }

    @Override
    public synchronized void log(String jobId, Integer type, String name, String executor, String beanName, String params, Integer status, String msg, Integer times) {
        this.jobManager.getScheduleJobLogService().log(jobId, type, name, executor, beanName, params, status, msg, times);
    }

    @Override
    public synchronized void log(String msg) {
        ScheduleJobEntity job = jobManager.getScheduleJob();
        String msgStr = String.format("[线程%s,流量%s]%s", Thread.currentThread().getName(), recordExchanger.getReaderMemorySizeString(), msg);
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

    public boolean isIncrement() {
        return increment;
    }

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }

    public String getIncrementColumn() {
        return incrementColumn;
    }

    public void setIncrementColumn(String incrementColumn) {
        this.incrementColumn = incrementColumn;
    }

    public boolean isIncrementAll() {
        return incrementAll;
    }

    public void setIncrementAll(boolean incrementAll) {
        this.incrementAll = incrementAll;
    }

    public Date getIncrementTime() {
        return incrementTime;
    }

    public void setIncrementTime(Date incrementTime) {
        this.incrementTime = incrementTime;
    }

    public Date getLastDataTime() {
        return lastDataTime;
    }

    public void setLastDataTime(Date lastDataTime) {
        this.lastDataTime = lastDataTime;
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
        protected abstract void reader();

        /**
         * 写出
         */
        protected void writer() {

        }
    }
}
