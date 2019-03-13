package com.anluy.datapig.job.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 定时器
 *
 * @date 2016年11月28日 下午12:54:44
 */
public class ScheduleJobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务调度参数key
     */
    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";

    /**
     * 任务id
     */
    private String jobId;
    /**
     * 任务类型
     */
    private Integer type;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 任务执行器名称
     */
    private String executor;

    /**
     * spring bean名称
     */
    private String beanName;

    /**
     * 参数
     */
    private String params;

    /**
     * 参数
     */
    private Map paramsMap;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 任务运行状态
     */
    private Integer runStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    private Long startTime;
    private Long prevFireTime;
    private Long nextFireTime;

    /**
     * 增量类型
     */
    private Integer increment;
    /**
     * 是否执行一次全量
     */
    private Integer incrementAll;
    /**
     * 增量类型
     */
    private String incrementColumn;

    private Date incrementTime;
    /**
     * 设置：任务id
     *
     * @param jobId 任务id
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * 获取：任务id
     *
     * @return Long
     */
    public String getJobId() {
        return jobId;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 设置：任务状态
     *
     * @param status 任务状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取：任务状态
     *
     * @return String
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置：cron表达式
     *
     * @param cronExpression cron表达式
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    /**
     * 获取：cron表达式
     *
     * @return String
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * 设置：创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取：创建时间
     *
     * @return Date
     */
    public Date getCreateTime() {
        return createTime;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Map getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(Map paramsMap) {
        this.paramsMap = paramsMap;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(Integer runStatus) {
        this.runStatus = runStatus;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getPrevFireTime() {
        return prevFireTime;
    }

    public void setPrevFireTime(Long prevFireTime) {
        this.prevFireTime = prevFireTime;
    }

    public Long getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Long nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Integer getIncrement() {
        return increment;
    }

    public void setIncrement(Integer increment) {
        this.increment = increment;
    }

    public Integer getIncrementAll() {
        return incrementAll;
    }

    public void setIncrementAll(Integer incrementAll) {
        this.incrementAll = incrementAll;
    }

    public String getIncrementColumn() {
        return incrementColumn;
    }

    public void setIncrementColumn(String incrementColumn) {
        this.incrementColumn = incrementColumn;
    }

    public Date getIncrementTime() {
        return incrementTime;
    }

    public void setIncrementTime(Date incrementTime) {
        this.incrementTime = incrementTime;
    }
}
