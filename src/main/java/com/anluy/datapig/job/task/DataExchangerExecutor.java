package com.anluy.datapig.job.task;

import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Executor;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.Plugin;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-12 12:18
 */
public class DataExchangerExecutor extends ScheduleExecutor {
    private static final Logger logger = LoggerFactory.getLogger(DataExchangerExecutor.class);
    private ExecutorService exec = Executor.executorService;
    private JobManager jobManager;
    private RecordExchanger recordExchanger = new RecordExchanger();
    private Plugin reader;
    private Plugin writer;

    public DataExchangerExecutor(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    @Override
    public Boolean execute() {
        try {
            Map plugin = (Map) jobManager.getScheduleJob().getParamsMap().get("plugin");
            if (plugin == null) {
                logger.error("plugin config is null");
                throw new DataPigException("plugin config is null");
            }
            Map readerConfig = (Map) plugin.get("reader");
            Map writerConfig = (Map) plugin.get("writer");
            if (readerConfig == null) {
                logger.error("reader config is null");
                throw new DataPigException("reader config is null");
            }
            if (writerConfig == null) {
                logger.error("writer config is null");
                throw new DataPigException("writer config is null");
            }

            Collection<Plugin> taskList = new ArrayList<Plugin>();
            String readerName = (String) readerConfig.get("name");
            String writerName = (String) writerConfig.get("name");
            if (StringUtils.isBlank(readerName)) {
                logger.error("reader plugin name is null");
                throw new DataPigException("reader plugin name is null");
            }
            if (StringUtils.isBlank(writerName)) {
                logger.error("writer plugin name is null");
                throw new DataPigException("writer plugin name is null");
            }
            Class readerClass = Class.forName(readerName);
            Class writerClass = Class.forName(writerName);
            reader = (Plugin) readerClass.getConstructor(new Class[]{JobManager.class, Map.class, RecordExchanger.class}).newInstance(jobManager, readerConfig, recordExchanger);
            writer = (Plugin) writerClass.getConstructor(new Class[]{JobManager.class, Map.class, RecordExchanger.class}).newInstance(jobManager, writerConfig, recordExchanger);
            taskList.add(reader);
            taskList.add(writer);

            List<Future<Boolean>> results = Lists.newArrayList();
            try {
                results = exec.invokeAll(taskList);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            for (Future<Boolean> result : results) {
                try {
                    result.get();
                } catch (ExecutionException e) {
                    throw new DataPigException(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            recordExchanger.close();
            return true;
        }catch (Exception e){
            throw new DataPigException(e);
        }finally {
            this.shutdown();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (reader != null) {
            reader.shutdown();
        }
        if (writer != null) {
            writer.shutdown();
        }
        if (recordExchanger != null) {
            recordExchanger.shutdown();
        }
    }

    @Override
    public String getMsg() {
        if(recordExchanger!=null){
            return " 读取："+recordExchanger.getReaderSize()+"条，写入："+recordExchanger.getWriterSize()+"条，总流量："+recordExchanger.getReaderMemorySizeString();
        }
        return " ";
    }
}
