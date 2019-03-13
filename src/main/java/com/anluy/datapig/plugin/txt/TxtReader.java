package com.anluy.datapig.plugin.txt;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Reader;
import com.anluy.datapig.plugin.core.element.*;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.core.exchanger.RecordSender;
import com.anluy.datapig.plugin.utils.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Txt文件读取插件
 *
 * @author hc.zeng
 * @create 2018-10-10 16:15
 */

public class TxtReader extends Reader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TxtReader.class);
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件夹路径
     */
    private String fileDir;
    /**
     * 文件前缀
     */
    private String prefix;
    /**
     * 文件后缀
     */
    private String suffix;

    /**
     * 列分隔符
     */
    private String separator;

    private String encoding;
    private List<File> fileList = new ArrayList<>();

    public TxtReader(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
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
        filePath = (String) params.get("filePath");
        fileDir = (String) params.get("fileDir");
        prefix = (String) params.get("prefix");
        suffix = (String) params.get("suffix");
        encoding = (String) params.get("encoding");
        separator = (String) params.get("separator");
        if (StringUtils.isBlank(filePath) && StringUtils.isBlank(fileDir)) {
            throw new DataPigException("TxtReader Plugin : filePath or fileDir is null!");
        }
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }
        if (StringUtils.isBlank(separator)) {
            separator = ",";
        }
        if (getJobManager().getScheduleJob().getIncrement() != 0) {
            ScheduleJobEntity entity = new ScheduleJobEntity();
            entity.setJobId(getJobManager().getScheduleJob().getJobId());
            entity.setIncrement(0);
            entity.setIncrementAll(0);
            entity.setIncrementColumn("");
            getJobManager().getScheduleJobService().update(entity);
        }

        log("读数据线程查询文件列表完成");
        return null;
    }


    @Override
    public Object start() {
        this.getFileList();
        TxtReader.Task task = new TxtReader.Task();
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
     * 获取文件列表
     */
    protected void getFileList() {
        if (StringUtils.isNotBlank(filePath)) {
            if (!filePath.toUpperCase().endsWith(".TXT")) {
                LOGGER.error(String.format("文件[%s]不是txt格式", filePath));
                throw new DataPigException(String.format("文件[%s]不是txt格式", filePath));
            }
            File file = new File(filePath);
            if (file.exists()) {
                fileList.add(file);
            } else {
                LOGGER.error(String.format("文件[%s]不存在", filePath));
                throw new DataPigException(String.format("文件[%s]不存在", filePath));
            }
        } else if (StringUtils.isNotBlank(fileDir)) {
            File dir = new File(fileDir);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (StringUtils.isNotBlank(prefix) && !name.toUpperCase().startsWith(prefix.toLowerCase())) {
                            return false;
                        }
                        if (StringUtils.isNotBlank(suffix) && !name.toUpperCase().endsWith(suffix.toUpperCase() + ".TXT")) {
                            return false;
                        }
                        if (!name.toUpperCase().endsWith(".TXT")) {
                            return false;
                        }
                        return true;
                    }
                });
                for (File f : files) {
                    fileList.add(f);
                }
            } else {
                LOGGER.error(String.format("文件目录[%s]不存在", dir));
                throw new DataPigException(String.format("文件目录[%s]不存在", dir));
            }
        }
        if (fileList.isEmpty()) {
            LOGGER.error(String.format("文件或目录[%s,%s]不存在", filePath, fileDir));
            throw new DataPigException(String.format("文件或目录[%s,%s]不存在", filePath, fileDir));
        }
    }

    protected int readerFile() {
        BufferedReader bufferedReader = null;
        long time = System.currentTimeMillis();
        AtomicInteger pcs = new AtomicInteger(0);
        AtomicInteger mite = new AtomicInteger(1);
        try {
            //开始读取数据
            for (File file : fileList) {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
                String title = bufferedReader.readLine();
                String line = bufferedReader.readLine();
                while (line != null) {
                    transportOneRecord(getRecordExchanger(), title, line);
                    long time2 = System.currentTimeMillis();
                    pcs.addAndGet(1);
                    if ((time2 - time) > 60000 * mite.get()) {
                        log("读取数据" + pcs.get() + "条");
                        mite.addAndGet(1);
                    }
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
            }
        } catch (Exception e) {
            throw new DataPigException(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LOGGER.error("关闭文件流失败", e);
                }
            }
        }
        return pcs.get();
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
                int pcs = readerFile();
                getRecordExchanger().terminate();
                log("读取数据完成，共" + pcs + "条");
            } catch (Exception e) {
                LOGGER.error("读取数据发生异常 :" + e.getMessage(), e);
                getRecordExchanger().shutdown();
                log("读取数据发生异常:" + e.getMessage());
                throw new DataPigException(e);
            }
        }
    }

    protected Record transportOneRecord(RecordSender recordSender, String title, String line) {
        Record record = buildRecord(recordSender, title, line);
        recordSender.sendToWriter(record);
        return record;
    }

    /**
     * 给子类重写的
     *
     * @param recordSender
     * @param titleStr
     * @param lineStr
     * @return
     */
    protected Record buildRecord(RecordSender recordSender, String titleStr, String lineStr) {
        Record record = recordSender.createRecord();
        try {
            String[] titles = titleStr.split(separator);
            String[] lines = lineStr.split(separator);
            if(lines.length > 1){
                for (int i = 0; i < titles.length ; i++) {
                    String title = titles[i];
                    Object value = lines.length > i ? lines[i] : null;
                    record(record,title,value);
                }
            }else {
                record(record,titles[0],lineStr);
            }

        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("read data " + record.toString() + " occur exception:", e);
            }
        }
        return record;
    }
    private void record(Record record,String title,Object value){
        Column column = null;
        if (value == null) {
            column = new StringColumn((String) value);
        } else {
            if (getFormatterMap().containsKey(title)) {
                Formatter formatter = getFormatterMap().get(title);
                value = formatter.formatter(value);
            }
            if (value instanceof String) {
                column = new StringColumn((String) value);
            } else if (value instanceof Long) {
                column = new LongColumn((Long) value);
            } else if (value instanceof Double) {
                column = new DoubleColumn((Double) value);
            } else if (value instanceof Date) {
                column = new DateColumn((Date) value);
            } else {
                throw new DataPigException(String.format("您的配置文件中的列配置信息有误. 因为DataPig 不支持数据库读取这种字段类型. 字段名:[%s]. 请尝试使用数据库函数将其转换能支持的类型 或者不同步该字段 .", title));
            }
        }
        column.setColumnName(title);
        if (getIncrementColumn() != null && column.getColumnName().toUpperCase().equals(getIncrementColumn().toUpperCase())) {
            setLastDataTime(column.asDate());
        }
        record.addColumn(column);
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
                            getFormatterMap().put(key, Formatter.STRING_TO_DATE(pattern));
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

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
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
}
