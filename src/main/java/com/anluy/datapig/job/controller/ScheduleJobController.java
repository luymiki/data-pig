package com.anluy.datapig.job.controller;

import com.alibaba.fastjson.JSON;
import com.anluy.datapig.dao.Page;
import com.anluy.datapig.entity.Result;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.service.ScheduleJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static javafx.scene.input.KeyCode.R;

/**
 * 定时任务
 * 
 * @date 2016年11月28日 下午2:16:40
 */
@Api(value = "", description = "调度任务管理接口")
@RestController
@RequestMapping("/api/admin/schedule")
public class ScheduleJobController {

	@Autowired
	private ScheduleJobService scheduleJobService;
	
	/**
	 * 定时任务列表
	 */
	@ApiOperation(value = "查询定时任务列表", response = Object.class)
	@RequestMapping(value = "/list",method = RequestMethod.POST)
	public Object list(@RequestParam(required = false) Map<String, Object> params){
		//查询列表数据
		Page<ScheduleJobEntity> page = new Page<ScheduleJobEntity>();
        page.setFilters(params);
        page.setSortColumns((String) params.get("sort"));
        page.setPageNumber((String) params.get("pageNum"));
        page.setPageSize((String) params.get("pageSize"));
		scheduleJobService.listPage(page);
		return ResponseEntity.ok(Result.success(page));
	}
	
	/**
	 * 定时任务信息
	 */
	@RequestMapping("/info/{jobId}")
	public Object info(@PathVariable("jobId") String jobId){
		ScheduleJobEntity schedule = scheduleJobService.get(jobId);
		return ResponseEntity.ok(Result.success(schedule));
	}
	
	/**
	 * 保存定时任务
	 */
	@ApiOperation(value = "保存任务", response = Object.class)
	@RequestMapping(value = "/save",method = RequestMethod.POST)
	public Object save(@RequestBody ScheduleJobEntity scheduleJob){
	    if(StringUtils.isNotBlank(scheduleJob.getJobId())){
	        return this.update(scheduleJob);
        }
		scheduleJobService.saveAndSchedule(scheduleJob);
		return ResponseEntity.ok(Result.success(scheduleJob));
	}
	
	/**
	 * 修改定时任务
	 */
	@RequestMapping(value = "/update",method = RequestMethod.POST)
	public Object update(@RequestBody ScheduleJobEntity scheduleJob){
        scheduleJob.setRunStatus(0);
		scheduleJobService.updateAndSchedule(scheduleJob);
		return ResponseEntity.ok(Result.success(scheduleJob));
	}
	
	/**
	 * 删除定时任务
	 */
	@RequestMapping(value = "/delete",method = RequestMethod.POST)
	public Object delete(@RequestParam String jobId){
		scheduleJobService.deleteAndSchedule(jobId);
		return ResponseEntity.ok(Result.success(jobId));
	}
	
	/**
	 * 立即执行任务
	 */
	@ApiOperation(value = "立即执行任务", response = Object.class)
	@RequestMapping(value = "/run",method = RequestMethod.POST)
	public Object run(@RequestParam String jobId){
		scheduleJobService.run(jobId);
		return ResponseEntity.ok(Result.success("启动定时任务成功"));
	}
	/**
	 * 开始调度任务
	 */
	@ApiOperation(value = "开始调度任务", response = Object.class)
	@RequestMapping(value = "/start",method = RequestMethod.POST)
	public Object start(@RequestParam String jobId){
		scheduleJobService.start(jobId);
		return ResponseEntity.ok(Result.success("启动定时任务成功"));
	}
	/**
	 * 停止定时任务
	 */
	@ApiOperation(value = "停止定时任务", response = Object.class)
	@RequestMapping(value = "/stop",method = RequestMethod.POST)
	public Object stop(@RequestParam String jobId){
		scheduleJobService.pause(jobId,1,2);
		return ResponseEntity.ok(Result.success("停止定时任务成功"));
	}

	/**
	 * 暂停定时任务
	 */
	@ApiOperation(value = "暂停定时任务", response = Object.class)
	@RequestMapping(value = "/pause",method = RequestMethod.POST)
	public Object pause(@RequestParam String jobId){
		scheduleJobService.pause(jobId,2,3);
		return ResponseEntity.ok(Result.success("暂停定时任务成功"));
	}


	
	/**
	 * 恢复定时任务
	 */
	@ApiOperation(value = "恢复定时任务", response = Object.class)
	@RequestMapping(value = "/resume",method = RequestMethod.POST)
	public Object resume(@RequestParam String jobId){
		scheduleJobService.resume(jobId);
		return ResponseEntity.ok(Result.success("恢复定时任务成功"));
	}

}
