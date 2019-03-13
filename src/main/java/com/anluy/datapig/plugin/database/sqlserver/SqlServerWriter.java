package com.anluy.datapig.plugin.database.sqlserver;

import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.database.DataBaseType;
import com.anluy.datapig.plugin.database.DataBaseWriter;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;

import java.util.Map;

/**
 * SqlServer数据库写插件
 *
 * @author hc.zeng
 * @create 2018-10-10 15:27
 */

public class SqlServerWriter extends DataBaseWriter {

    public SqlServerWriter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager,params, recordExchanger);
    }

    @Override
    public Object init(Map params) {
        this.setDataBase(DataBaseType.MSSQL);
        return super.init(params);
    }

}
