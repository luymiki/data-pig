package com.anluy.datapig.plugin.txt;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Executor;
import com.anluy.datapig.plugin.core.Wirter;
import com.anluy.datapig.plugin.core.element.Column;
import com.anluy.datapig.plugin.core.element.Record;
import com.anluy.datapig.plugin.core.element.TerminateRecord;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.elasticsearch.utils.RestClientService;
import com.anluy.datapig.plugin.utils.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * txtw文件写入插件
 *
 * @author hc.zeng
 * @create 2018-10-25 16:30
 */

public class TxtWriter extends Wirter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TxtWriter.class);
    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 列分隔符
     */
    private String separator;

    private String encoding;

    private AtomicLong atomicLong = new AtomicLong();

    public TxtWriter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    @Override
    public Boolean call() throws Exception {
        this.execute();
        return true;
    }

    @Override
    public Object init(Map params) {
        filePath = (String) params.get("filePath");
        encoding = (String) params.get("encoding");
        separator = (String) params.get("separator");

        if (StringUtils.isBlank(filePath)) {
            throw new DataPigException("TxtWriter Plugin : host is null!");
        }
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }
        if (StringUtils.isBlank(separator)) {
            separator = ",";
        }
        return null;
    }

    @Override
    public Object start() {
        Task task = new Task();
        task.writer();
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

    protected BufferedWriter getBufferedWriter(){
        try{
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), getEncoding()));
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
            throw new DataPigException(e);
        }
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
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = getBufferedWriter();
                log(String.format("写数据线连创建文件[%s]完成", filePath));
                long time = System.currentTimeMillis();
                int mite = 1;
                boolean writerTitle = false;
                while (!isShutdown()) {
                    Record record = getRecordExchanger().getFromReader();
                    if (record instanceof TerminateRecord) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("写数据线程获取到结束标记");
                        }
                        getRecordExchanger().shutdown();
                        setShutdown(true);
                        break;
                    }

                    if (!writerTitle) {
                        writerTitle(bufferedWriter, record);
                        writerTitle = true;
                    }

                    writer(bufferedWriter, record);
                    bufferedWriter.newLine();
                    //每分钟记录一次日志
                    long time2 = System.currentTimeMillis();
                    if ((time2 - time) > 60000 * mite) {
                        log("总共写入数据" + atomicLong.get() + "条");
                        mite++;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                getRecordExchanger().shutdown();
                log("数据写入文件发生异常:" + e.getMessage());
                throw new DataPigException(e);
            } finally {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("数据写入文件完成，总共写入数据" + atomicLong.get() + "条");
                }
                log("数据写入文件完成，总共写入数据" + atomicLong.get() + "条");
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        LOGGER.info("关闭文件流失败");
                    }

                }
            }
        }

        private void writerTitle(BufferedWriter bufferedWriter, Record record) throws Exception {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < record.getColumnNumber(); i++) {
                Column column = record.getColumn(i);
                String key = column.getColumnName().toLowerCase();
                if (i > 0) {
                    stringBuffer.append(separator);
                }
                stringBuffer.append(key);
            }
            bufferedWriter.write(stringBuffer.toString());
            bufferedWriter.flush();
            atomicLong.addAndGet(1);
        }

        private void writer(BufferedWriter bufferedWriter, Record record) throws Exception {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < record.getColumnNumber(); i++) {
                Column column = record.getColumn(i);
                String key = column.getColumnName().toLowerCase();
                Object value = null;
                if (getFormatterMap().containsKey(key)) {
                    Formatter formatter = getFormatterMap().get(key);
                    value = formatter.formatter(column.asObject());
                } else {
                    value = column.asString();
                }
                if (i > 0) {
                    stringBuffer.append(separator);
                }
                stringBuffer.append(value);
            }
            bufferedWriter.write(stringBuffer.toString());
            bufferedWriter.flush();
            atomicLong.addAndGet(1);
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public AtomicLong getAtomicLong() {
        return atomicLong;
    }

    public void setAtomicLong(AtomicLong atomicLong) {
        this.atomicLong = atomicLong;
    }
}
