package com.anluy.datapig.plugin.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.Reader;
import com.anluy.datapig.plugin.elasticsearch.utils.Configuration;
import com.anluy.datapig.plugin.elasticsearch.utils.RestClientService;
import com.anluy.datapig.plugin.core.element.*;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.core.exchanger.RecordSender;
import com.anluy.datapig.plugin.utils.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库读取插件
 *
 * @author hc.zeng
 * @create 2018-10-10 16:15
 */

public class ElasticSearchReader extends Reader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchReader.class);
    private String host;
    private String username;
    private String password;
    private String indexName;
    private String typeName;
    private String dsl;
    private String prefix;
    private RestClientService restClientService;

    public ElasticSearchReader(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    @Override
    public Boolean call() throws Exception {
        this.execute();
        return true;
    }

    /**
     * @param params
     * @return
     */
    @Override
    public Object init(Map params) {
        host = (String) params.get("host");
        username = (String) params.get("username");
        password = (String) params.get("password");
        dsl = (String) params.get("dsl");
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

        //创建而是连接
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("读数据线程获取ElasticSearch连接");
        }
        restClientService = new RestClientService(host, username, password);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("读数据线程获取ElasticSearch连接完成");
        }
        log("读数据线连接ElasticSearch完成");
        //初始化格式函数
        this.formatter();
        return null;
    }

    @Override
    public Object start() {
        ElasticSearchReader.Task task = new ElasticSearchReader.Task();
        task.reader();
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

    /**
     * 任务实现
     */
    public class Task extends Reader.Task {

        @Override
        public Boolean call() throws Exception {
            reader();
            return true;
        }

        @Override
        protected void reader() {
            try {
                String dsl = this.incrementDsl();
                log("读取数据DSL:" + dsl);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("start query dsl => " + dsl);
                }
                Map<String, Map<String, Configuration>> mappings = restClientService.getMapping(new String[]{indexName},typeName);
                if(mappings == null || mappings.isEmpty()){
                    throw new Exception(String.format("index[%s]不存在",indexName));
                }
                Configuration mapping = mappings.get(indexName).get(typeName);
                if(mapping == null){
                    throw new Exception(String.format("type[%s]不存在",typeName));
                }
                Configuration properties = mapping.getConfiguration("properties");
                if(properties == null){
                    throw new Exception("properties为空");
                }

                Set keySet = ((JSONObject)properties.getInternal()).keySet();

                //开始读取数据
                long time = System.currentTimeMillis();
                AtomicInteger pcs = new AtomicInteger(0);
                AtomicInteger mite = new AtomicInteger(1);

                restClientService.scroll(dsl, "5", new RestClientService.TimeWindowCallBack() {
                    @Override
                    public void process(List<Map> recordDataList) {
                        for (Map map : recordDataList) {
                            transportOneRecord(getRecordExchanger(),keySet, map);
                            long time2 = System.currentTimeMillis();
                            pcs.addAndGet(1);
                            if ((time2 - time) > 60000 * mite.get()) {
                                log("读取数据" + pcs.get() + "条");
                                mite.addAndGet(1);
                            }
                        }
                    }
                }, indexName, typeName, null, null);
                getRecordExchanger().terminate();
                log("读取数据完成，共" + pcs + "条");
            } catch (Exception e) {
                LOGGER.error("read database fail :" + e.getMessage(), e);
                getRecordExchanger().shutdown();
                log("读取数据发生异常:" + e.getMessage());
                throw new DataPigException(e);
            } finally {
                if (restClientService != null) {
                    restClientService.close();
                }
            }
        }

        protected Record transportOneRecord(RecordSender recordSender,Set keySet, Map map) {
            Record record = buildRecord(recordSender,keySet, map);
            recordSender.sendToWriter(record);
            return record;
        }

        protected Record buildRecord(RecordSender recordSender,Set keySet, Map map) {
            Record record = recordSender.createRecord();
            try {
                keySet.forEach(key->{
                    String ks = (String) key;
                    if ("_id".equals(ks) || "parent_id".equals(ks)) {
                        return;
                    }
                    if (StringUtils.isNotBlank(prefix)) {
                        ks = ((String) key).replaceFirst(prefix, "");
                    }

                    Object value = map.get(key);
                    Column column = null;
                    if (value == null) {
                        column = new StringColumn((String) value);
                    } else {
                        if (getFormatterMap().containsKey(ks)) {
                            Formatter formatter = getFormatterMap().get(ks);
                            value = formatter.formatter(value);
                        }

                        if (value instanceof String) {
                            column = new StringColumn((String) value);
                        } else if (value instanceof Integer) {
                            column = new LongColumn((Integer) value);
                        } else if (value instanceof Long) {
                            column = new LongColumn((Long) value);
                        } else if (value instanceof Float) {
                            column = new DoubleColumn((Float) value);
                        } else if (value instanceof Double) {
                            column = new DoubleColumn((Double) value);
                        } else if (value instanceof BigDecimal) {
                            column = new DoubleColumn((BigDecimal) value);
                        } else if (value instanceof BigInteger) {
                            column = new LongColumn((BigInteger) value);
                        } else if (value instanceof Date) {
                            column = new DateColumn((Date) value);
                        } else if (value instanceof JSONArray) {
                            StringBuffer sb = new StringBuffer(100);
                            for (Object obj : (JSONArray) value) {
                                sb.append(obj.toString()).append(" ");
                            }
                            column = new StringColumn(sb.toString().trim());
                        } else {
                            throw new DataPigException(String.format("您的配置文件中的列配置信息有误. 因为DataPig 不支持读取这种字段类型. 字段名:[%s].", ks));
                        }
                    }
                    if(getMapping().containsKey(ks)){
                        ks = getMapping().getString(ks);
                    }
                    column.setColumnName(ks);
                    if (getIncrementColumn() != null && column.getColumnName().toUpperCase().equals(getIncrementColumn().toUpperCase())) {
                        setLastDataTime(column.asDate());
                    }
                    record.addColumn(column);
                });

            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("read data " + record.toString() + " occur exception:", e);
                }
                throw e;
            }
            return record;
        }

        /**
         * 生成增量查询的sql
         *
         * @return
         */
        private String incrementDsl() {
            if (StringUtils.isBlank(dsl)) {
                dsl = "{\"size\":1000,\"query\":{\"match_all\":{}}}";
            }
            if (isIncrement()) {
                JSONObject root = JSON.parseObject(dsl);
                JSONObject query = root.getJSONObject("query");
                JSONObject bool = (JSONObject) query.remove("match_all");

                Map map = new HashMap();
                Map range = new HashMap();
                Map must = new HashMap();
                map.put("from", DateFormatUtils.format(getIncrementTime(), "yyyy-MM-dd HH:mm:ss"));
                map.put("to", null);
                map.put("include_lower", true);
                map.put("include_upper", true);
                range.put(getIncrementColumn(), map);
                must.put("range", range);
                bool.put("must", must);
                query.put("bool", bool);

                return root.toJSONString();
            }
            return dsl;
        }
    }


    /**
     * 格式化
     *
     * @return
     */
    @Override
    public void formatter() {
        this.getFormats().forEach((key, val) -> {
            JSONObject format = (JSONObject) val;
            if (format != null && !format.isEmpty()) {
                for (Map.Entry<String, Object> entry : format.entrySet()) {
                    String type = entry.getKey();
                    String pattern = (String) entry.getValue();
                    switch (type) {
                        case "date": {
                            getFormatterMap().put(key,Formatter.STRING_TO_DATE(pattern) );
                            break;
                        }
                        case "join": {
                            getFormatterMap().put(key, Formatter.JOIN(pattern));
                            break;
                        }
                    }
                }
            }
        });
    }
}
