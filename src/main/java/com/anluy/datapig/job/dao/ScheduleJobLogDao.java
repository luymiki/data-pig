package com.anluy.datapig.job.dao;

import com.anluy.datapig.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务日志
 *
 * @date 2016年12月1日 下午10:30:02
 */
@Mapper
public interface ScheduleJobLogDao<ScheduleJobLogEntity, Long> extends BaseDao {
    void removeByJobId(String jobId);
}
