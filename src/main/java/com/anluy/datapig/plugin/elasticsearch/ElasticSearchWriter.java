package com.anluy.datapig.plugin.elasticsearch;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Executor;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.Wirter;
import com.anluy.datapig.plugin.elasticsearch.utils.RestClientService;
import com.anluy.datapig.plugin.core.element.Column;
import com.anluy.datapig.plugin.core.element.Record;
import com.anluy.datapig.plugin.core.element.TerminateRecord;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.utils.Formatter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ElasticSearch写入插件
 *
 * @author hc.zeng
 * @create 2018-10-25 16:30
 */

public class ElasticSearchWriter extends Wirter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchWriter.class);
    private int batchSize = 500;
    private ExecutorService exec;
    private static final int ThreadSize = 3;
    private String host;
    private String username;
    private String password;
    private String indexName;
    private String prefix;
    private String typeName;
    private AtomicLong atomicLong = new AtomicLong();

    public ElasticSearchWriter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    @Override
    public Boolean call() throws Exception {
        this.execute();
        return true;
    }

    @Override
    public Object init(Map params) {
        host = (String) params.get("host");
        username = (String) params.get("username");
        password = (String) params.get("password");
        String batchSize = (String) params.get("batchSize");
        indexName = (String) params.get("indexName");
        typeName = (String) params.get("typeName");
        prefix = (String) params.get("prefix");

        if (StringUtils.isBlank(host)) {
            throw new DataPigException("ElasticSearchWriter Plugin : host is null!");
        }
        if (StringUtils.isBlank(username)) {
            throw new DataPigException("ElasticSearchWriter Plugin : username is null!");
        }
        if (StringUtils.isBlank(password)) {
            throw new DataPigException("ElasticSearchWriter Plugin : password is null!");
        }
        if (StringUtils.isBlank(indexName)) {
            throw new DataPigException("ElasticSearchWriter Plugin : indexName is null!");
        }
        if (StringUtils.isBlank(typeName)) {
            throw new DataPigException("ElasticSearchWriter Plugin : typeName is null!");
        }
        if (StringUtils.isNotBlank(batchSize)) {
            try {
                this.batchSize = Integer.valueOf(batchSize);
            } catch (Exception e) {
                throw new DataPigException("batchSize not's a number", e);
            }
        }
        return null;
    }

    @Override
    public Object start() {
        //初始化线程池
        exec = Executor.executorService;
        Collection<Task> taskList = new ArrayList<Task>();
        for (int i = 0; i < ThreadSize; i++) {
            Task task = new Task();
            taskList.add(task);
        }
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
                //Thread.currentThread().interrupt();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    @Override
    public Object shutdown() {
        this.setShutdown(true);
        return null;
    }

    @Override
    public Object end() {
        shutdown();
        return null;
    }

    public class Task extends Wirter.Task {
        private RestClientService restClientService;

        @Override
        public Boolean call() throws Exception {
            writer();
            return null;
        }

        @Override
        protected void writer() {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("启动写数据线程");
            }
            try {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("写数据线程获取ElasticSearch连接");
                }
                restClientService = new RestClientService(host, username, password);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("写数据线程获取ElasticSearch连接完成");
                }
                log("写数据线连接ElasticSearch完成");

                long time = System.currentTimeMillis();
                int mite = 1;
                while (!isShutdown()) {
                    Record record = null;
                    List<Record> dataList = new ArrayList<>();
                    for (int i = 0; i < batchSize; i++) {
                        record = getRecordExchanger().getFromReader();
                        if (record instanceof TerminateRecord) {
                            break;
                        }
                        dataList.add(record);
                    }
                    save(dataList);

                    //每分钟记录一次日志
                    long time2 = System.currentTimeMillis();
                    if ((time2 - time) > 60000 * mite) {
                        log("总共写入数据" + atomicLong.get() + "条");
                        mite++;
                    }

                    if (record instanceof TerminateRecord) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("写数据线程获取到结束标记");
                        }
                        getRecordExchanger().shutdown();
                        setShutdown(true);
                        break;
                    }
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                setShutdown(true);
                getRecordExchanger().shutdown();
                log("数据入ElasticSearch发生异常:" + e.getMessage());
                throw new DataPigException(e);
            } finally {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("数据入ElasticSearch完成，总共写入数据" + atomicLong.get() + "条");
                }
                log("数据入ElasticSearch完成，总共写入数据" + atomicLong.get() + "条");
                if (restClientService != null) {
                    restClientService.close();
                }
            }
        }

        private void save(List<Record> dataList) throws Exception {
            if (dataList.size() > 0) {
                List<Map> listMap = new ArrayList<>();
                dataList.forEach(record -> {
                    Map map = new HashMap();
                    for (int i = 0; i < record.getColumnNumber(); i++) {
                        Column column = record.getColumn(i);
                        String key = column.getColumnName().toLowerCase();
                        Object value = null;
                        if (getFormatterMap().containsKey(key)) {
                            try {
                                Formatter formatter = getFormatterMap().get(key);
                                value = formatter.formatter(column.asObject());
                            } catch (Exception e) {
                                throw new DataPigException(e);
                            }

                        } else {
                            value = column.asObject();
                        }
                        if(getMapping().containsKey(key)){
                            key = getMapping().getString(key);
                        }
                        if (StringUtils.isNotBlank(prefix)) {
                            if("_id".equals(key) || "_parent".equals(key)){
                                map.put(key, value);
                            }else {
                                map.put(prefix + key, value);
                            }
                        } else {
                            map.put(key, value);
                        }
                    }
                    listMap.add(map);
                });

                restClientService.batchSave(listMap, indexName, typeName);
                atomicLong.addAndGet(dataList.size());
                dataList.clear();
                dataList = null;
            }

        }
    }

    /**
     * 格式化
     *
     * @return
     */
    @Override
    public void formatter() {
        getFormats().forEach((key, val) -> {
            JSONObject format = (JSONObject) val;
            if (format != null && !format.isEmpty()) {
                for (Map.Entry<String, Object> entry : format.entrySet()) {
                    String type = entry.getKey();
                    String pattern = (String) entry.getValue();
                    switch (type) {
                        case "date": {
                            getFormatterMap().put(key, Formatter.DATE_TO_STRING(pattern));
                            break;
                        }
                        case "split": {
                            getFormatterMap().put(key, Formatter.SPLIT(pattern));
                            break;
                        }
                    }
                }
            }
        });
    }

}
