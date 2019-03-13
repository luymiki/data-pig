package com.anluy.datapig.plugin.database;

import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.Reader;
import com.anluy.datapig.plugin.core.element.*;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.core.exchanger.RecordSender;
import com.anluy.datapig.plugin.utils.DBUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;

/**
 * 数据库读取插件
 *
 * @author hc.zeng
 * @create 2018-10-10 16:15
 */

public abstract class DataBaseReader extends Reader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseReader.class);
    protected final byte[] EMPTY_CHAR_ARRAY = new byte[0];
    private DataBaseType dataBase;
    private Connection connection;
    private String encoding;
    private String sql;

    public DataBaseReader(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    @Override
    public Boolean call() throws Exception {
        this.execute();
        return true;
    }

    @Override
    public Object init(Map params) {
        String url = (String) params.get("url");
        String username = (String) params.get("username");
        String password = (String) params.get("password");

        sql = (String) params.get("sql");
        encoding = (String) params.get("encoding");
        if (StringUtils.isBlank(url)) {
            throw new DataPigException("DataBaseReader Plugin : url is null!");
        }
        if (StringUtils.isBlank(username)) {
            throw new DataPigException("DataBaseReader Plugin : username is null!");
        }
        if (StringUtils.isBlank(password)) {
            throw new DataPigException("DataBaseReader Plugin : password is null!");
        }

        if (StringUtils.isBlank(sql)) {
            throw new DataPigException("DataBaseReader Plugin : sql is null!");
        }

        if (dataBase == null) {
            throw new DataPigException("DataBaseReader Plugin : dataBase is null!");
        }

        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("get connection start ");
            }
            connection = DBUtil.getConnection(dataBase, url, username, password);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("get connection OK ");
            }
            log("读数据线程连接数据库完成");
        } catch (Exception e) {
            throw new DataPigException(e);
        }

        return null;
    }

    @Override
    public Object start() {
        DataBaseReader.Task task = new DataBaseReader.Task();
        task.reader();
//        exec = Executor.executorService;
//        Future<Boolean> result = exec.submit(task);
//        try {
//            result.get();
//        } catch (ExecutionException e) {
//            throw new DataPigException(e);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
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

    public class Task extends Reader.Task {

        @Override
        public Boolean call() throws Exception {
            reader();
            return true;
        }

        @Override
        protected void reader() {
            ResultSet resultSet = null;
            Statement statement = null;
            try {

                String sql = incrementSql();
                log("读取数据SQL:" + sql);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("start query sql => " + sql);
                }
                statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                statement.setFetchSize(5000);
                if(dataBase == DataBaseType.MYSQL){
                    statement.setFetchSize(Integer.MIN_VALUE);
                    ((com.mysql.jdbc.Statement)statement).enableStreamingResults();
                }
                resultSet = statement.executeQuery(sql);
                ResultSetMetaData metaData = resultSet.getMetaData();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("query ok，sender data");
                }

                //开始读取数据
                long time = System.currentTimeMillis();
                int pcs = 0;
                int mite = 1;
                while (!isShutdown() && resultSet.next()) {
                    //转换数据并往通道中写入
                    this.transportOneRecord(getRecordExchanger(), resultSet, metaData);
                    long time2 = System.currentTimeMillis();
                    pcs++;
                    //每分钟记录一次日志信息
                    if ((time2 - time) > 60000 * mite) {
                        log("读取数据" + pcs + "条");
                        mite++;
                    }
                }
                //往通道中写入一个完成标记的数据行
                getRecordExchanger().terminate();
                getRecordExchanger().terminate();
                getRecordExchanger().terminate();
                log("读取数据完成，共" + pcs + "条");
            } catch (Exception e) {
                LOGGER.error("read database fail :" + e.getMessage(), e);
                getRecordExchanger().shutdown();
                log("读取数据发生异常:" + e.getMessage());
                throw new DataPigException(e);
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        LOGGER.error("close resultSet fail :" + e.getMessage(), e);
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        LOGGER.error("close statement fail :" + e.getMessage(), e);
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        LOGGER.error("close connection fail :" + e.getMessage(), e);
                    }
                }
            }
        }

        /**
         * 转换数据
         *
         * @param recordSender
         * @param resultSet
         * @param metaData
         * @return
         */
        protected Record transportOneRecord(RecordSender recordSender, ResultSet resultSet, ResultSetMetaData metaData) {
            Record record = buildRecord(recordSender, resultSet, metaData);
            //往通道中写入一条记录
            recordSender.sendToWriter(record);
            return record;
        }

        /**
         * 创建一行数据对象,并转换数据格式
         *
         * @param recordSender
         * @param rs
         * @param metaData
         * @return
         */
        protected Record buildRecord(RecordSender recordSender, ResultSet rs, ResultSetMetaData metaData) {
            Record record = recordSender.createRecord();
            try {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    Column column = null;
                    switch (metaData.getColumnType(i)) {
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGNVARCHAR:
                            String rawData;
                            if (StringUtils.isBlank(encoding)) {
                                rawData = rs.getString(i);
                            } else if(rs.getBytes(i)==null){
                                rawData = null;
                            }else{
                                rawData = new String(rs.getBytes(i), encoding);
                            }
                            column = new StringColumn(rawData);
                            break;

                        case Types.CLOB:
                        case Types.NCLOB:
                            column = new StringColumn(rs.getString(i));
                            break;

                        case Types.SMALLINT:
                        case Types.TINYINT:
                        case Types.INTEGER:
                        case Types.BIGINT:
                            column = new LongColumn(rs.getString(i));
                            break;

                        case Types.NUMERIC:
                        case Types.DECIMAL:
                            column = new DoubleColumn(rs.getString(i));
                            break;

                        case Types.FLOAT:
                        case Types.REAL:
                        case Types.DOUBLE:
                            column = new DoubleColumn(rs.getString(i));
                            break;

                        case Types.TIME:
                            column = new DateColumn(rs.getTime(i));
                            break;

                        // for mysql bug, see http://bugs.mysql.com/bug.php?id=35115
                        case Types.DATE:
                            if (metaData.getColumnTypeName(i).equalsIgnoreCase("year")) {
                                column = new LongColumn(rs.getInt(i));
                            } else {
                                column = new DateColumn(rs.getDate(i));
                            }
                            break;

                        case Types.TIMESTAMP:
                            column = new DateColumn(rs.getTimestamp(i));
                            break;

                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.BLOB:
                        case Types.LONGVARBINARY:
                            column = new BytesColumn(rs.getBytes(i));
                            break;

                        // warn: bit(1) -> Types.BIT 可使用BoolColumn
                        // warn: bit(>1) -> Types.VARBINARY 可使用BytesColumn
                        case Types.BOOLEAN:
                        case Types.BIT:
                            column = new BoolColumn(rs.getBoolean(i));
                            break;

                        case Types.NULL:
                            String stringData = null;
                            if (rs.getObject(i) != null) {
                                stringData = rs.getObject(i).toString();
                            }
                            column = new StringColumn(stringData);
                            break;

                        default:
                            throw new DataPigException(
                                    String.format(
                                            "您的配置文件中的列配置信息有误. 因为DataPig 不支持数据库读取这种字段类型. 字段名:[%s], 字段名称:[%s], 字段Java类型:[%s]. 请尝试使用数据库函数将其转换能支持的类型 或者不同步该字段 .",
                                            metaData.getColumnLabel(i),
                                            metaData.getColumnType(i),
                                            metaData.getColumnClassName(i)));
                    }
                    column.setColumnName(metaData.getColumnLabel(i));
                    //设置增量抽取的时间字段的最后一条记录的值
                    if (getIncrementColumn() != null && column.getColumnName().toUpperCase().equals(getIncrementColumn().toUpperCase())) {
                        setLastDataTime(column.asDate());
                    }
                    record.addColumn(column);
                }
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("read data " + record.toString()
                            + " occur exception:", e);
                }
            }
            return record;
        }

        /**
         * 生成增量查询的sql
         *
         * @return
         */
        private String incrementSql() {
            String SQL = sql.toUpperCase();
            if (isIncrement()) {
                StringBuffer sb = new StringBuffer();
                sb.append(getIncrementColumn()).append(" >").append(strToDate(DateFormatUtils.format(getIncrementTime(), "yyyy-MM-dd HH:mm:ss.SSS")));
                if (SQL.indexOf("WHERE") > 0) {
                    sb.append(" AND ");
                    SQL = SQL.replaceFirst("WHERE", "WHERE " + sb.toString());
                }else {
                    SQL = SQL + " WHERE " + sb.toString();
                }

            }
            if(StringUtils.isNotBlank(getIncrementColumn())){
                if (SQL.indexOf("ORDER") < 0) {
                    SQL = SQL + " ORDER BY " + getIncrementColumn() + " ASC";
                }
            }
            return SQL;
        }

        private String strToDate(String incrementTime) {
            StringBuffer sb = new StringBuffer();
            switch (dataBase) {
                case MYSQL: {
                    sb.append("str_to_date('").append(incrementTime).append("','%Y-%m-%d %H:%i:%s.%f') ");
                    break;
                }
                case ORACLE: {
                    sb.append("TO_TIMESTAMP('").append(incrementTime).append("','YYYY-MM-DD HH24:MI:SS.FF') ");
                    break;
                }
                case MSSQL: {
                    sb.append("CONVERT(DATETIME,'").append(incrementTime).append("',121) ");
                    break;
                }
            }
            return sb.toString();
        }

    }

    @Override
    public void formatter() {

    }

    public void setDataBase(DataBaseType dataBase) {
        this.dataBase = dataBase;
    }
}
