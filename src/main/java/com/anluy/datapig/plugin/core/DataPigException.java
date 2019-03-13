package com.anluy.datapig.plugin.core;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-10 16:40
 */

public class DataPigException extends RuntimeException {
    public DataPigException() {
    }

    public DataPigException(String message) {
        super(message);
    }

    public DataPigException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataPigException(Throwable cause) {
        super(cause);
    }

    public DataPigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
