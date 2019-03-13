package com.anluy.datapig.dao.dialect;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-20 18:52
 */

public class MySql5Dialect extends Dialect {
    protected static final String SQL_END_DELIMITER = ";";

    public MySql5Dialect() {
    }

    public String getLimitString(String sql, boolean hasOffset) {
        return MySql5PageHepler.getLimitString(sql, -1, -1);
    }

    @Override
    public String getLimitString(String sql, int offset, int limit) {
        return MySql5PageHepler.getLimitString(sql, offset, limit);
    }

    public boolean supportsLimit() {
        return true;
    }
}