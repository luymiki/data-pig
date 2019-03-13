package com.anluy.datapig.job.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时执行日志
 * 
 * @date 2016年12月1日 下午10:26:18
 */
public class ScheduleJobLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 日志id
	 */
	private Long logId;

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
	 * 任务状态    0：成功    1：失败
	 */
	private Integer status;
	
	/**
	 * 日志信息
	 */
	private String msg;
	
	/**
	 * 耗时(单位：毫秒)
	 */
	private Integer times;
	
	/**
	 * 创建时间
	 */
	private Date createTime;

	public Long getLogId() {
		return logId;
	}

	public void setLogId(Long logId) {
		this.logId = logId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public ScheduleJobLogEntity() {
	}

	public ScheduleJobLogEntity(String jobId, Integer type, String name, String executor, String beanName, String params, Integer status, String msg, Integer times, Date createTime) {
		this.jobId = jobId;
		this.type = type;
		this.name = name;
		this.executor = executor;
		this.beanName = beanName;
		this.params = params;
		this.status = status;
		this.msg = msg;
		this.times = times;
		this.createTime = createTime;
	}
}
