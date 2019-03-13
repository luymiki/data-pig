package com.anluy.datapig.plugin.ftp;

import com.anluy.datapig.plugin.core.DataPigException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-11-08 11:39
 */

public class FTPUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPUtils.class);

    public static FTPClient getFTPClient(String host, int port, String userName, String password) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(host, port);
            ftpClient.login(userName, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new DataPigException(e);
        }
    }

    public static void closeFTPClient(FTPClient ftpClient) {
        if (ftpClient != null) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                LOGGER.error("关闭FTP连接失败", e);
            }
        }
    }
}
