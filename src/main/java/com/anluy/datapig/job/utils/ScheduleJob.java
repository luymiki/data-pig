package com.anluy.datapig.job.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.entity.ScheduleJobLogEntity;
import com.anluy.datapig.job.service.ScheduleJobLogService;
import com.anluy.datapig.job.service.ScheduleJobService;
import com.anluy.datapig.job.task.DataExchangerExecutor;
import com.anluy.datapig.job.task.ScheduleExecutor;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.utils.SpringContextUtils;
import com.anluy.datapig.utils.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 定时任务
 *
 * @date 2016年11月30日 下午12:44:21
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ScheduleJob extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ExecutorService service = Executors.newSingleThreadExecutor();
    private ScheduleExecutor scheduleExecutor;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String jsonJob = context.getMergedJobDataMap().getString(ScheduleJobEntity.JOB_PARAM_KEY);
        ScheduleJobEntity scheduleJob = JSONObject.parseObject(jsonJob, ScheduleJobEntity.class);

        //获取spring bean
        ScheduleJobLogService scheduleJobLogService = (ScheduleJobLogService) SpringContextUtils.getBean("scheduleJobLogService");
        ScheduleJobService scheduleJobService = (ScheduleJobService) SpringContextUtils.getBean("scheduleJobService");
        scheduleJob = scheduleJobService.get(scheduleJob.getJobId());

        //数据库保存执行记录
        ScheduleJobLogEntity log = new ScheduleJobLogEntity();
        log.setJobId(scheduleJob.getJobId());
        log.setName(scheduleJob.getName());
        log.setExecutor(scheduleJob.getExecutor());
        log.setType(scheduleJob.getType());
        log.setBeanName(scheduleJob.getBeanName());
        log.setParams(scheduleJob.getParams());

        //任务开始时间
        long startTime = System.currentTimeMillis();

        try {
            scheduleJobService.updateStatus(scheduleJob.getJobId(), null, 1);
            //执行任务
            logger.info("任务准备执行，任务ID：" + scheduleJob.getJobId());
            JobManager jobManager = new JobManager(scheduleJob, scheduleJobService, scheduleJobLogService);
            JSONObject obj = JSON.parseObject(scheduleJob.getParams());
            scheduleJob.setParamsMap(obj);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(scheduleJob.getExecutor()) && DataExchangerExecutor.class.getName().endsWith(scheduleJob.getExecutor())) {
                scheduleExecutor = new DataExchangerExecutor(jobManager);
            } else {
                scheduleExecutor = new ScheduleExecutor(jobManager);
            }
            scheduleExecutor.execute();

            //任务执行总时长
            long times = System.currentTimeMillis() - startTime;
            log.setTimes((int) times);
            //任务状态    0：成功    1：失败
            log.setStatus(0);
            log.setMsg("任务执行完毕，总共耗时：" + ScheduleUtils.millisecondStringify(times) + scheduleExecutor.getMsg());
            logger.info(log.getMsg());
            scheduleJobService.updateStatus(scheduleJob.getJobId(), null, 2);
        } catch (Exception e) {
            logger.error("任务执行失败，任务ID：" + scheduleJob.getJobId(), e);
            //任务执行总时长
            long times = System.currentTimeMillis() - startTime;
            log.setTimes((int) times);
            //任务状态    0：成功    1：失败
            log.setStatus(1);
            log.setMsg("任务执行失败，" + StringUtils.substring(e.toString(), 0, 4000));
            scheduleJobService.pause(scheduleJob.getJobId(), 1, 4);
        } finally {
            log.setCreateTime(new Date());
            scheduleJobLogService.save(log);
            System.runFinalization();
            System.gc();
        }
    }

    public void shutdown() {
        scheduleExecutor.shutdown();
    }
}
