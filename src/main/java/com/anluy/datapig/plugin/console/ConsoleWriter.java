package com.anluy.datapig.plugin.console;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Wirter;
import com.anluy.datapig.plugin.core.element.Column;
import com.anluy.datapig.plugin.core.element.Record;
import com.anluy.datapig.plugin.core.element.TerminateRecord;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.utils.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Console输出插件
 *
 * @author hc.zeng
 * @create 2018-10-25 16:30
 */

public class ConsoleWriter extends Wirter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleWriter.class);
    private AtomicLong atomicLong = new AtomicLong();

    public ConsoleWriter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    @Override
    public Boolean call() throws Exception {
        this.execute();
        return true;
    }

    @Override
    public Object init(Map params) {
        return null;
    }

    @Override
    public Object start() {
        new Task().writer();
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
        @Override
        public Boolean call() throws Exception {
            writer();
            return null;
        }

        @Override
        protected void writer() {
            try {
                long time = System.currentTimeMillis();
                int mite = 1;
                while (!isShutdown()) {
                    Record record = getRecordExchanger().getFromReader();
                    atomicLong.addAndGet(1);
                    print(record);
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
                getRecordExchanger().shutdown();
                log("数据在Console输出发生异常:" + e.getMessage());
                throw new DataPigException(e);
            } finally {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("数据在Console输出完成，总共输出数据" + atomicLong.get() + "条");
                }
                log("数据在Console输出完成，总共输出数据" + atomicLong.get() + "条");
            }
        }

        private void print(Record record) throws Exception {
            Map map = new HashMap();
            for (int i = 0; i < record.getColumnNumber(); i++) {
                Column column = record.getColumn(i);
                String key = column.getColumnName().toLowerCase();
                Object value = null;
                if (getFormatterMap().containsKey(key)) {
                    Formatter formatter = getFormatterMap().get(key);
                    value = formatter.formatter(column.asObject());
                } else {
                    value = column.asObject();
                }
                map.put(key, value);
            }
            System.out.println(JSON.toJSONString(map));
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
                    }
                }
            }
        });
    }

}
