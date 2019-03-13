package com.anluy.datapig.job.service;

import com.anluy.datapig.job.entity.ScheduleJobEntity;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-11-05 11:06
 */
public class JobManager {
    private ScheduleJobEntity scheduleJob;
    private ScheduleJobService scheduleJobService;
    private ScheduleJobLogService scheduleJobLogService;

    public JobManager(ScheduleJobEntity scheduleJob, ScheduleJobService scheduleJobService, ScheduleJobLogService scheduleJobLogService) {
        this.scheduleJob = scheduleJob;
        this.scheduleJobService = scheduleJobService;
        this.scheduleJobLogService = scheduleJobLogService;
    }

    public ScheduleJobEntity getScheduleJob() {
        return scheduleJob;
    }

    public void setScheduleJob(ScheduleJobEntity scheduleJob) {
        this.scheduleJob = scheduleJob;
    }

    public ScheduleJobService getScheduleJobService() {
        return scheduleJobService;
    }

    public void setScheduleJobService(ScheduleJobService scheduleJobService) {
        this.scheduleJobService = scheduleJobService;
    }

    public ScheduleJobLogService getScheduleJobLogService() {
        return scheduleJobLogService;
    }

    public void setScheduleJobLogService(ScheduleJobLogService scheduleJobLogService) {
        this.scheduleJobLogService = scheduleJobLogService;
    }
}
