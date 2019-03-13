package com.anluy.datapig.plugin.database;

import com.alibaba.fastjson.JSON;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Executor;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.Wirter;
import com.anluy.datapig.plugin.core.element.Record;
import com.anluy.datapig.plugin.core.element.TerminateRecord;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.utils.DBUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据库写入插件
 *
 * @author hc.zeng
 * @create 2018-10-10 16:30
 */
public abstract class DataBaseWriter extends Wirter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseWriter.class);
    private DataBaseType dataBase;
    private int batchSize = 500;
    private ExecutorService exec;
    private static final int ThreadSize = 5;
    private String url;
    private String username;
    private String password;
    private String tableName;
    private AtomicLong atomicLong = new AtomicLong();

    public DataBaseWriter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    @Override
    public Boolean call() throws Exception {
        this.execute();
        return true;
    }

    /**
     * 初始化参数
     *
     * @param params
     * @return
     */
    @Override
    public Object init(Map params) {
        url = (String) params.get("url");
        username = (String) params.get("username");
        password = (String) params.get("password");
        String batchSize = (String) params.get("batchSize");
        tableName = (String) params.get("tableName");
        //校验必须参数
        if (StringUtils.isBlank(url)) {
            throw new DataPigException("DataBaseWriter Plugin : url is null!");
        }
        if (StringUtils.isBlank(username)) {
            throw new DataPigException("DataBaseWriter Plugin : username is null!");
        }
        if (StringUtils.isBlank(password)) {
            throw new DataPigException("DataBaseWriter Plugin : password is null!");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new DataPigException("DataBaseWriter Plugin : tableName is null!");
        }
        if (dataBase == null) {
            throw new DataPigException("DataBaseWriter Plugin : dataBase is null!");
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


    /**
     * 开始执行
     *
     * @return
     */
    @Override
    public Object start() {
        //创建线程池
        exec = Executor.executorService;
        //创建多个任务，并批量执行。并等等执行结果
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
        //获取执行结果
        for (Future<Boolean> result : results) {
            try {
                result.get();
            } catch (ExecutionException e) {
                //如果线程内部有错误，往上层抛出。
                throw new DataPigException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    @Override
    public Object shutdown() {
        setShutdown(true);
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
    public class Task extends Wirter.Task {
        private String insertSql;
        private Connection connection;

        @Override
        public Boolean call() throws Exception {
            writer();
            return null;
        }

        /**
         * 任务的具体实现
         */
        @Override
        protected void writer() {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("启动写数据线程");
            }
            try {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("写数据线程获取数据库连接");
                }
                connection = DBUtil.getConnection(dataBase, url, username, password);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("写数据线程获取数据库连接完成");
                }

                log("连接数据库完成");

                long time = System.currentTimeMillis();
                int mite = 1;
                while (!isShutdown()) {
                    Record record = null;
                    List<Record> dataList = new ArrayList<>();
                    //获取一批数据
                    for (int i = 0; i < batchSize; i++) {
                        //从通道中获取一条记录
                        record = getRecordExchanger().getFromReader();
                        if (record instanceof TerminateRecord) {
                            break;
                        }
                        if (StringUtils.isBlank(insertSql)) {
                            getInsertSql(record);
                            log("生成插入SQL:" + insertSql);
                        }
                        dataList.add(record);
                    }
                    //批量保存
                    save(dataList);

                    //每分钟记录一次日志
                    long time2 = System.currentTimeMillis();
                    if ((time2 - time) > 60000 * mite) {
                        log("总共写入数据" + atomicLong.get() + "条");
                        mite++;
                    }
                    //如果读取到结束标记，将通道关闭，并将当前线程标记为关闭,
                    if (record instanceof TerminateRecord) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("写数据线程获取到结束标记");
                        }
                        //标记通道为关闭，让其他线程也能根据关闭状态一起关闭了
                        getRecordExchanger().shutdown();
                        setShutdown(true);
                        break;
                    }
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                getRecordExchanger().shutdown();
                log("数据入库发生异常:" + e.getMessage());
                throw new DataPigException(e);
            } finally {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("数据入库完成，总共写入数据" + atomicLong.get() + "条");
                }
                log("数据入库完成，总共写入数据" + atomicLong.get() + "条");
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        LOGGER.error("关闭数据库连接失败", e);
                    }
                }
            }
        }

        /**
         * 生成INSERT 的sql语句
         *
         * @param record
         * @return
         */
        private String getInsertSql(Record record) {
            if (StringUtils.isBlank(insertSql)) {
                StringBuffer sb = new StringBuffer();
                StringBuffer sbCol = new StringBuffer();
                for (int i = 0; i < record.getColumnNumber(); i++) {
                    if (i > 0) {
                        sb.append(",");
                        sbCol.append(",");
                    }
                    sb.append(record.getColumn(i).getColumnName());
                    sbCol.append("?");
                }
                insertSql = "INSERT INTO " + tableName + "(" + sb.toString() + ") VALUES (" + sbCol.toString() + ")";
            }
            return insertSql;
        }

        /**
         * 批量保存数据
         *
         * @param dataList
         * @throws SQLException
         */
        private void save(List<Record> dataList) throws SQLException {
            if (dataList.size() > 0) {
                PreparedStatement prepareStatement = connection.prepareStatement(insertSql);
                connection.setAutoCommit(false);
                for (Record record : dataList) {
                    for (int i = 0; i < record.getColumnNumber(); i++) {
                        prepareStatement.setObject(i + 1, record.getColumn(i).asObject());
                    }
                    prepareStatement.addBatch();
                }
                try {
                    prepareStatement.executeBatch();
                } catch (Exception e) {
                    LOGGER.info("批量保存失败，转为单条记录提交。", e);
                    connection.rollback();
                    for (Record record : dataList) {
                        for (int i = 0; i < record.getColumnNumber(); i++) {
                            Object o = record.getColumn(i).asObject();
                            if (o instanceof Double) {
                                prepareStatement.setObject(i + 1, record.getColumn(i).asObject(), Types.DOUBLE);
                            } else {
                                prepareStatement.setObject(i + 1, record.getColumn(i).asObject());
                            }
                        }
                        try {
                            prepareStatement.execute();
                        } catch (Exception e2) {
                            LOGGER.error("单条记录入库失败！\n" + record.toString() + "\n" + e2.getMessage(), e);
                            throw e2;
                        }
                    }
                }
                connection.commit();
                prepareStatement.close();
                atomicLong.addAndGet(dataList.size());
                dataList.clear();
                dataList = null;
            }

        }

    }

    @Override
    public void formatter() {

    }

    public void setDataBase(DataBaseType dataBase) {
        this.dataBase = dataBase;
    }


}
