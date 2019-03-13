package com.anluy.datapig.job.service;

import com.anluy.datapig.job.entity.ScheduleJobLogEntity;
import com.anluy.datapig.plugin.core.PluginLogger;
import com.anluy.datapig.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 定时任务日志
 * 
 * @date 2016年12月1日 下午10:34:48
 */
public interface ScheduleJobLogService extends BaseService<ScheduleJobLogEntity,Long>,PluginLogger {
    void removeByJobId(String jobId);
}
