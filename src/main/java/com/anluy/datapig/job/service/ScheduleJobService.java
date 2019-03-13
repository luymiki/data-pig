package com.anluy.datapig.job.service;

import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 定时任务
 * 
 * @date 2016年11月28日 上午9:55:32
 */
public interface ScheduleJobService extends BaseService<ScheduleJobEntity,String>  {

	ScheduleJobEntity saveAndSchedule(ScheduleJobEntity scheduleJob);
	ScheduleJobEntity updateAndSchedule(ScheduleJobEntity scheduleJob);
	/**
	 * 删除定时任务
	 */
	void deleteAndSchedule(String jobId);

	void updateStatus(String jobId,Integer status,Integer runStatus);

	/**
	 * 立即执行
	 */
	void run(String jobIds);
	/**
	 * 开始调度任务
	 */
	void start(String jobIds);

	/**
	 * 暂停运行
	 */
	void pause(String jobIds,Integer status, Integer runStatus);
	
	/**
	 * 恢复运行
	 */
	void resume(String jobIds);
}
