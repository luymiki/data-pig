package com.anluy.datapig.job.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.anluy.datapig.dao.BaseDao;
import com.anluy.datapig.job.dao.ScheduleJobLogDao;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.entity.ScheduleJobLogEntity;
import com.anluy.datapig.job.service.ScheduleJobLogService;
import com.anluy.datapig.plugin.core.PluginLogger;
import com.anluy.datapig.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hc.zeng
 */
@Service("scheduleJobLogService")
public class ScheduleJobLogServiceImpl extends BaseServiceImpl<ScheduleJobLogEntity, Long> implements ScheduleJobLogService {

    @Autowired
    private ScheduleJobLogDao scheduleJobLogDao;

    @Override
    public void log(String msg) {

    }

    @Override
    public void log(String jobId, Integer type, String name, String executor, String beanName, String params, Integer status, String msg, Integer times) {
        ScheduleJobLogEntity entity = new ScheduleJobLogEntity(jobId, type, name, executor, beanName, params, status, msg, times, new Date());
        this.save(entity);
        //System.out.println(JSON.toJSONString(entity));
    }

    @Override
    public BaseDao<ScheduleJobLogEntity, Long> getDao() {
        return scheduleJobLogDao;
    }

    @Override
    public void preSave(ScheduleJobLogEntity var1) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByJobId(String jobId) {
        this.scheduleJobLogDao.removeByJobId(jobId);
    }
}
