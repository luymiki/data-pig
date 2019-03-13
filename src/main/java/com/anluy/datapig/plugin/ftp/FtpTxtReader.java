package com.anluy.datapig.plugin.ftp;

import com.alibaba.fastjson.JSONObject;
import com.anluy.datapig.job.entity.ScheduleJobEntity;
import com.anluy.datapig.job.service.JobManager;
import com.anluy.datapig.plugin.core.DataPigException;
import com.anluy.datapig.plugin.core.Reader;
import com.anluy.datapig.plugin.core.element.*;
import com.anluy.datapig.plugin.core.exchanger.RecordExchanger;
import com.anluy.datapig.plugin.core.exchanger.RecordSender;
import com.anluy.datapig.plugin.txt.TxtReader;
import com.anluy.datapig.plugin.utils.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FtpTxt文件读取插件
 *
 * @author hc.zeng
 * @create 2018-10-10 16:15
 */

public class FtpTxtReader extends TxtReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpTxtReader.class);
    private String host;
    private int port;
    private String userName;
    private String password;
    private FTPClient ftpClient;
    private List<String> fileList = new ArrayList<>();

    public FtpTxtReader(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
        super(jobManager, params, recordExchanger);
    }

    /**
     * @param params
     * @return
     */
    @Override
    public Object init(Map params) {
        host = (String) params.get("host");
        String port = (String) params.get("port");
        userName = (String) params.get("userName");
        password = (String) params.get("password");
        if (StringUtils.isBlank(host)) {
            throw new DataPigException("FtpTxtReader Plugin : filePath or host is null!");
        }
        if (StringUtils.isBlank(userName)) {
            throw new DataPigException("FtpTxtReader Plugin : userName is null!");
        }
        if (StringUtils.isNotBlank(port)) {
            try {
                this.port = Integer.valueOf(port);
            } catch (Exception e) {
                throw new DataPigException("FtpTxtReader Plugin : port is not a number!");
            }
        }
        if (StringUtils.isBlank(password)) {
            throw new DataPigException("FtpTxtReader Plugin : password is null!");
        }
        super.init(params);
        return null;
    }

    @Override
    public Object start() {
        this.ftpClient = FTPUtils.getFTPClient(host, port, userName, password);
        return super.start();
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

    /**
     * 获取文件列表
     */
    @Override
    protected void getFileList() {
        try {
            if (StringUtils.isNotBlank(getFilePath())) {
                String filePath = getFilePath();
                if (!filePath.toUpperCase().endsWith(".TXT")) {
                    LOGGER.error(String.format("文件[%s]不是txt格式", filePath));
                    throw new DataPigException(String.format("文件[%s]不是txt格式", filePath));
                }
                File file = new File(filePath);
                String work = file.getParentFile().getPath().replace("\\", "/");
                // 获取ftp上的文件
                if (ftpClient.changeWorkingDirectory(work)) {
                    FTPFile[] files = ftpClient.listFiles(work, new FTPFileFilter() {
                        @Override
                        public boolean accept(FTPFile f) {
                            if (f.getName().equals(file.getName())) {
                                return true;
                            }
                            return false;
                        }
                    });
                    if (files.length == 1) {
                        fileList.add(files[0].getName());
                    } else {
                        LOGGER.error(String.format("文件[%s]不存在", filePath));
                        throw new DataPigException(String.format("文件[%s]不存在", filePath));
                    }
                } else {
                    LOGGER.error(String.format("文件目录[%s]不存在", file.getParentFile().getAbsolutePath()));
                    throw new DataPigException(String.format("文件目录[%s]不存在", file.getParentFile().getAbsolutePath()));
                }

            } else if (StringUtils.isNotBlank(getFileDir())) {
                String fileDir = getFileDir();
                if (ftpClient.changeWorkingDirectory(fileDir)) {
                    FTPFile[] files = ftpClient.listFiles(fileDir, new FTPFileFilter() {
                        @Override
                        public boolean accept(FTPFile file) {
                            if (StringUtils.isNotBlank(getPrefix()) && !file.getName().toUpperCase().startsWith(getPrefix().toLowerCase())) {
                                return false;
                            }
                            if (StringUtils.isNotBlank(getSuffix()) && !file.getName().toUpperCase().endsWith(getSuffix().toUpperCase() + ".TXT")) {
                                return false;
                            }
                            if (!file.getName().toUpperCase().endsWith(".TXT")) {
                                return false;
                            }
                            return true;
                        }
                    });
                    for (FTPFile f : files) {
                        fileList.add(f.getName());
                    }
                } else {
                    LOGGER.error(String.format("文件目录[%s]不存在", fileDir));
                    throw new DataPigException(String.format("文件目录[%s]不存在", fileDir));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new DataPigException(e);
        }
        if (fileList.isEmpty()) {
            LOGGER.error(String.format("文件或目录[%s,%s]不存在", getFilePath(), getFileDir()));
            throw new DataPigException(String.format("文件或目录[%s,%s]不存在", getFilePath(), getFileDir()));
        }
    }

    @Override
    protected int readerFile() {
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        long time = System.currentTimeMillis();
        AtomicInteger pcs = new AtomicInteger(0);
        AtomicInteger mite = new AtomicInteger(1);
        try {
            //开始读取数据
            for (String fileName : fileList) {
                inputStream = ftpClient.retrieveFileStream(fileName);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, getEncoding()));
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
                inputStream.close();
                bufferedReader = null;
                inputStream = null;
                ftpClient.completePendingCommand();
            }
        } catch (Exception e) {
            throw new DataPigException(e.getMessage(), e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LOGGER.error("关闭文件流失败", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("关闭文件流失败", e);
                }
            }
        }
        return pcs.get();
    }
}
