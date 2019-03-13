package com.anluy.datapig.job.service.impl;

import java.util.*;

import javax.annotation.PostConstruct;

import com.anluy.datapig.dao.BaseDao;
import com.anluy.datapig.job.dao.ScheduleJobDao;
import com.anluy.datapig.job.dao.ScheduleJobLogDao;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.service.ScheduleJobService;
import com.anluy.datapig.job.utils.ScheduleUtils;
import com.anluy.datapig.service.impl.BaseServiceImpl;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("scheduleJobService")
public class ScheduleJobServiceImpl extends BaseServiceImpl<ScheduleJobEntity, String> implements ScheduleJobService {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private ScheduleJobDao schedulerJobDao;
    @Autowired
    private ScheduleJobLogDao scheduleJobLogDao;

    @PostConstruct
    @Override
    public BaseDao getDao() {
        return schedulerJobDao;
    }

    /**
     * 项目启动时，初始化定时器
     */
    @PostConstruct
    public void init() {
//		List<ScheduleJobEntity> scheduleJobList = schedulerJobDao.queryList(new HashMap<>());
//		for(ScheduleJobEntity scheduleJob : scheduleJobList){
//			CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getJobId());
//            //如果不存在，则创建
//            if(cronTrigger == null) {
//                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
//            }else {
//                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
//            }
//		}
    }


    @Override
    public void preSave(ScheduleJobEntity var1) {
        if (StringUtils.isBlank(var1.getJobId())) {
            var1.setJobId(UUID.randomUUID().toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduleJobEntity saveAndSchedule(ScheduleJobEntity scheduleJob) {
        preSave(scheduleJob);
        scheduleJob.setCreateTime(new Date());
        //类型
        if (scheduleJob.getType() == null) {
            scheduleJob.setType(1);
        }
        //状态默认是启动的
        if (scheduleJob.getStatus() == null) {
            scheduleJob.setStatus(0);
        }
        schedulerJobDao.save(scheduleJob);
        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
        if (scheduleJob.getStatus() == 1) {
            ScheduleUtils.pauseJob(scheduler, scheduleJob.getJobId());
        }
        return scheduleJob;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduleJobEntity updateAndSchedule(ScheduleJobEntity scheduleJob) {
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
        schedulerJobDao.update(scheduleJob);
        return scheduleJob;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String jobId, Integer status, Integer runStatus) {
        ScheduleJobEntity entity = new ScheduleJobEntity();
        entity.setJobId(jobId);
        entity.setStatus(status);
        entity.setRunStatus(runStatus);
        schedulerJobDao.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAndSchedule(String jobId) {
        scheduleJobLogDao.removeByJobId(jobId);
        ScheduleUtils.deleteScheduleJob(scheduler, jobId);
        //删除数据
        schedulerJobDao.remove(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String jobId) {
        ScheduleJobEntity scheduleJob = this.get(jobId);
        ScheduleUtils.run(scheduler, scheduleJob);
        updateStatus(jobId, null, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(String jobId) {
        ScheduleJobEntity scheduleJob = this.get(jobId);
        updateStatus(jobId, 0, 0);
        scheduleJob.setStatus(0);
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
        //this.resume(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(String jobId, Integer status, Integer runStatus) {
        ScheduleUtils.pauseJob(scheduler, jobId);
        ScheduleJobEntity scheduleJob = this.get(jobId);
        scheduleJob.setJobId(jobId);
        if (scheduleJob.getStatus() != 1) {
            scheduleJob.setStatus(status);
        }
        scheduleJob.setRunStatus(runStatus);
        schedulerJobDao.update(scheduleJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resume(String jobId) {
        ScheduleUtils.resumeJob(scheduler, jobId);
        this.updateStatus(jobId, 0, 0);
        //updateBatch(jobIds, ScheduleStatus.NORMAL.getValue());
    }

}
