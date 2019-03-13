package com.anluy.datapig.job.task;

import com.anluy.datapig.job.service.ScheduleJobLogService;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.utils.SpringContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-12 12:18
 */
public class ScheduleExecutor {
    private Object target;
    private Method method;
    private JobManager jobManager;
    private volatile boolean shutdown = false;

    public ScheduleExecutor() {
    }

    public ScheduleExecutor(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public Boolean execute() {
        try {
            String beanName = jobManager.getScheduleJob().getBeanName();
            //是全路径
            if (beanName.indexOf(".") > 0) {
                Class clazz = Class.forName(beanName);
                this.target = SpringContextUtils.getBean(clazz);
                if (target == null) {
                    target = clazz.getConstructor().newInstance();
                }
            } else {
                this.target = SpringContextUtils.getBean(beanName);
            }
            if(target == null){
                throw new DataPigException("找不到任务类 "+ beanName);
            }
            if(target instanceof DataPigTask){
                DataPigTask dataPigTask = (DataPigTask) target;
                dataPigTask.setJobManager(jobManager);
                dataPigTask.setParams(jobManager.getScheduleJob().getParamsMap());
                dataPigTask.execute();
            }else {
                throw new DataPigException("不支持的任务类 "+ target.getClass().getName());
            }
        } catch (NoSuchMethodException e) {
            throw new DataPigException("执行定时任务失败", e);
        } catch (IllegalAccessException e) {
            throw new DataPigException("执行定时任务失败", e);
        } catch (InstantiationException e) {
            throw new DataPigException("执行定时任务失败", e);
        } catch (InvocationTargetException e) {
            throw new DataPigException("执行定时任务失败", e);
        } catch (ClassNotFoundException e) {
            throw new DataPigException("执行定时任务失败", e);
        }
        return true;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void shutdown() {
        this.shutdown = true;
    }

    public String getMsg() {
        return "";
    }
}
