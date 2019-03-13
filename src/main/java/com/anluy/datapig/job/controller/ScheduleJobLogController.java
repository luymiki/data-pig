package com.anluy.datapig.job.controller;

import com.anluy.datapig.dao.Page;
import com.anluy.datapig.entity.Result;
import com.anluy.datapig.job.entity.ScheduleJobLogEntity;
import com.anluy.datapig.job.service.ScheduleJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 定时任务日志
 * 
 * @date 2016年12月1日 下午10:39:52
 */
@RestController
@RequestMapping("/api/admin/schedule/log")
public class ScheduleJobLogController {
	@Autowired
	private ScheduleJobLogService scheduleJobLogService;
	
	/**
	 * 定时任务日志列表
	 */
	@RequestMapping("/list")
	public Object list(@RequestParam Map<String, Object> params){
		//查询列表数据
		Page<ScheduleJobLogEntity> page = new Page<ScheduleJobLogEntity>();
		page.setFilters(params);
		page.setSortColumns((String) params.get("sort"));
		page.setPageNumber((String) params.get("pageNum"));
		page.setPageSize((String) params.get("pageSize"));
		scheduleJobLogService.listPage(page);
		return ResponseEntity.ok(Result.success(page));
	}
	
	/**
	 * 定时任务日志信息
	 */
	@RequestMapping("/deleteAll/{jobId}")
	public Object deleteAll(@PathVariable("jobId") String jobId){
		scheduleJobLogService.removeByJobId(jobId);
		return ResponseEntity.ok(Result.success("删除成功"));
	}
	/**
	 * 定时任务日志信息
	 */
	@RequestMapping("/delete/{logId}")
	public Object delete(@PathVariable("logId") Long logId){
		scheduleJobLogService.remove(logId);
		return ResponseEntity.ok(Result.success("删除成功"));
	}
}
