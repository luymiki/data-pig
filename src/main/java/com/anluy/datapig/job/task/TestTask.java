package com.anluy.datapig.job.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 测试定时任务(演示Demo，可删除)
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-11-05 17:02
 */
@Component("testTask")
public class TestTask extends DataPigTask{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void task() {
		logger.info(String.format("我是任务[%s]，正在被执行。",this.getJobManager().getScheduleJob().getName()));
	}

	@Override
	public void shutdown() {
		this.setShutdown(true);
	}
}
