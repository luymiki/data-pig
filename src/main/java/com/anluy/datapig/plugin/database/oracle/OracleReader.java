package com.anluy.datapig.plugin.database.oracle;

import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.database.DataBaseReader;
import com.anluy.datapig.plugin.database.DataBaseType;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;

import java.util.Map;

/**
 * oracle数据库读插件
 *
 * @author hc.zeng
 * @create 2018-10-10 15:27
 */

public class OracleReader extends DataBaseReader {


    public OracleReader(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager,params, recordExchanger);
    }

    @Override
    public Object init(Map params) {
        this.setDataBase(DataBaseType.ORACLE);
        return super.init(params);
    }

}
