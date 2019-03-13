package com.anluy.datapig.plugin.ftp;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Wirter;
import com.anluy.datapig.plugin.core.element.Column;
import com.anluy.datapig.plugin.core.element.Record;
import com.anluy.datapig.plugin.core.element.TerminateRecord;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.elasticsearch.utils.RestClientService;
import com.anluy.datapig.plugin.txt.TxtWriter;
import com.anluy.datapig.plugin.utils.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ftp txtw文件写入插件
 *
 * @author hc.zeng
 * @create 2018-10-25 16:30
 */

public class FtpTxtWriter extends TxtWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpTxtWriter.class);
    private String host;
    private int port = 21;
    private String userName;
    private String password;
    private FTPClient ftpClient;

    public FtpTxtWriter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    @Override
    public Object init(Map params) {
        host = (String) params.get("host");
        String port = (String) params.get("port");
        userName = (String) params.get("userName");
        password = (String) params.get("password");
        if (StringUtils.isBlank(host)) {
            throw new DataPigException("FtpTxtWriter Plugin : filePath or host is null!");
        }
        if (StringUtils.isBlank(userName)) {
            throw new DataPigException("FtpTxtWriter Plugin : userName is null!");
        }
        if (StringUtils.isNotBlank(port)) {
            try {
                this.port = Integer.valueOf(port);
            } catch (Exception e) {
                throw new DataPigException("FtpTxtWriter Plugin : port is not a number!");
            }
        }
        if (StringUtils.isBlank(password)) {
            throw new DataPigException("FtpTxtWriter Plugin : password is null!");
        }
        super.init(params);
        return null;
    }

    @Override
    public Object start() {
        super.start();
        File file = new File("/temp.temp");
        if (!file.exists()) {
            log("写入临时文件失败");
            throw new DataPigException("写入临时文件失败");
        }
        FileInputStream inputStream = null;
        try {
            this.ftpClient = FTPUtils.getFTPClient(host, port, userName, password);
            inputStream = new FileInputStream(file);
            ftpClient.appendFile(getFilePath(), inputStream);
            log("临时文件上传FTP完成");
        } catch (Exception e) {
            log("临时文件上传到ftp失败");
            throw new DataPigException("临时文件上传到ftp失败");
        }finally {
            if(inputStream !=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("关闭文件流失败",e);
                }
            }
            file.delete();
        }
        return null;
    }

    @Override
    public Object shutdown() {
        FTPUtils.closeFTPClient(ftpClient);
        ftpClient = null;
        return super.shutdown();
    }

    @Override
    public Object end() {
        shutdown();
        return null;
    }

    @Override
    protected BufferedWriter getBufferedWriter() {
        try {
            File file = new File("/temp.temp");
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), getEncoding()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new DataPigException(e);
        }
    }

}
