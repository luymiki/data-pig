package com.anluy.datapig.dao.dialect;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-20 18:52
 */


public abstract class Dialect {
    public Dialect() {
    }

    public abstract String getLimitString(String var1, int var2, int var3);

    public static enum Type {
        MYSQL,
        ORACLE;

        private Type() {
        }
    }
}
