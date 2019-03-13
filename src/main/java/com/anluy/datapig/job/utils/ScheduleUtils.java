package com.anluy.datapig.job.utils;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import org.quartz.*;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 定时任务工具类
 *
 * @date 2016年11月30日 下午12:44:59
 */
public class ScheduleUtils {
    private final static String JOB_NAME = "TASK_";

    /**
     * 获取触发器key
     */
    public static TriggerKey getTriggerKey(String jobId) {
        return TriggerKey.triggerKey(JOB_NAME + jobId);
    }

    /**
     * 获取jobKey
     */
    public static JobKey getJobKey(String jobId) {
        return JobKey.jobKey(JOB_NAME + jobId);
    }

    /**
     * 获取表达式触发器
     */
    public static CronTrigger getCronTrigger(Scheduler scheduler, String jobId) {
        try {
            return (CronTrigger) scheduler.getTrigger(getTriggerKey(jobId));
        } catch (SchedulerException e) {
            throw new RuntimeException("获取定时任务CronTrigger出现异常", e);
        }
    }

    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, ScheduleJobEntity scheduleJob) {
        try {
            //构建job信息
            JobDetail jobDetail = JobBuilder.newJob(ScheduleJob.class).withIdentity(getJobKey(scheduleJob.getJobId())).build();

            //表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();

            //按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(scheduleJob.getJobId())).withSchedule(scheduleBuilder).build();

            //放入参数，运行时的方法可以获取
            jobDetail.getJobDataMap().put(ScheduleJobEntity.JOB_PARAM_KEY, JSONObject.toJSONString(scheduleJob));

            scheduler.scheduleJob(jobDetail, trigger);

            //暂停任务
            if (scheduleJob.getStatus() == 1) {
                pauseJob(scheduler, scheduleJob.getJobId());
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("创建定时任务失败", e);
        }
    }

    /**
     * 更新定时任务
     */
    public static void updateScheduleJob(Scheduler scheduler, ScheduleJobEntity scheduleJob) {
        try {
            deleteScheduleJob(scheduler,scheduleJob.getJobId());
            createScheduleJob(scheduler,scheduleJob);
        } catch (Exception e) {
            throw new RuntimeException("更新定时任务失败", e);
        }
    }

    /**
     * 立即执行任务
     */
    public static void run(Scheduler scheduler, ScheduleJobEntity scheduleJob) {
        try {
            //参数
            //JobDataMap dataMap = new JobDataMap();
            //dataMap.put(ScheduleJobEntity.JOB_PARAM_KEY, JSONObject.toJSONString(scheduleJob));
            //scheduler.triggerJob(getJobKey(scheduleJob.getJobId()), dataMap);
            scheduler.triggerJob(getJobKey(scheduleJob.getJobId()));
        } catch (SchedulerException e) {
            throw new RuntimeException("立即执行定时任务失败", e);
        }
    }

    /**
     * 暂停任务
     */
    public static void pauseJob(Scheduler scheduler, String jobId) {
        try {
            JobKey jobKey = getJobKey(jobId);
            List<JobExecutionContext> jobExecutionContexts = scheduler.getCurrentlyExecutingJobs();
            jobExecutionContexts.forEach(jobExecutionContext -> {
                Job job = jobExecutionContext.getJobInstance();
                JobKey jobKey2 = jobExecutionContext.getJobDetail().getKey();
                if (jobKey2.getName().equals(jobKey.getName()) && job instanceof ScheduleJob) {
                    ScheduleJob scheduleJob = (ScheduleJob) job;
                    scheduleJob.shutdown();
                }
            });
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("暂停定时任务失败", e);
        }
    }

    /**
     * 恢复任务
     */
    public static void resumeJob(Scheduler scheduler, String jobId) {
        try {
            scheduler.resumeJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new RuntimeException("恢复定时任务失败", e);
        }
    }

    /**
     * 删除定时任务
     */
    public static void deleteScheduleJob(Scheduler scheduler, String jobId) {
        try {
            scheduler.deleteJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new RuntimeException("删除定时任务失败", e);
        }
    }

    private final static long SECOND_IN_TIME = 1000;

    private final static long MINUTE_IN_TIME = 60 * SECOND_IN_TIME;

    private final static long HOUR_IN_TIME = 60 * MINUTE_IN_TIME;

    private final static long DAY_IN_TIME = 24 * HOUR_IN_TIME;

    private final static DecimalFormat df = new DecimalFormat("0.00");

    public static String millisecondStringify(long timeNumber) {

        StringBuffer str = new StringBuffer();
        long t = 0;
        if (timeNumber > DAY_IN_TIME) {
            t = timeNumber / DAY_IN_TIME;
            timeNumber = timeNumber - (t * DAY_IN_TIME);
            str.append(t).append("天");
        }
        if (timeNumber > HOUR_IN_TIME) {
            t = timeNumber / HOUR_IN_TIME;
            timeNumber = timeNumber - (t * HOUR_IN_TIME);
            str.append(t).append("小时");
        }
        if (timeNumber > MINUTE_IN_TIME) {
            t = timeNumber / MINUTE_IN_TIME;
            timeNumber = timeNumber - (t * MINUTE_IN_TIME);
            str.append(t).append("分钟");
        }
        if (timeNumber > SECOND_IN_TIME) {
            t = timeNumber / SECOND_IN_TIME;
            timeNumber = timeNumber - (t * SECOND_IN_TIME);
            str.append(t).append("秒");
        }
        str.append(timeNumber).append("毫秒");
        return str.toString();
    }
}
