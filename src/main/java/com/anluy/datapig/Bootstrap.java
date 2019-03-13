package com.anluy.datapig;

import com.anluy.datapig.plugin.core.element.ColumnCast;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by hc.zeng 2017-2-20.
 */
@SpringBootApplication
//@EnableCaching
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);

//        ColumnCast.bind();
//        RecordExchanger recordExchanger = new RecordExchanger();
//        Map readerMap = new HashMap();
//        Map writerMap = new HashMap();
//        readerMap.put("url","jdbc:oracle:thin:@68.64.9.188:1521:jzdb");
//        readerMap.put("username","jzck");
//        readerMap.put("password","jzck");
//        readerMap.put("sql","select * from qx_fkzjmx_source t");
//        readerMap.put("batchSize","500");
//
//        writerMap.put("url","jdbc:oracle:thin:@68.64.9.188:1521:jzdb");
//        writerMap.put("username","jzck");
//        writerMap.put("password","jzck");
//        OracleReader oracleReader = new OracleReader(readerMap,recordExchanger);
//        OracleWriter oracleWriter = new OracleWriter(writerMap,recordExchanger);
//        oracleReader.execute();
//        oracleWriter.execute();
    }
}