package com.anluy.datapig.plugin.database;

/**
 * 数据库类型枚举
 *
 * @author hc.zeng 055371
 * @create 2018-10-10 15:18
 */
public enum DataBaseType {

    ORACLE("oracle", "oracle.jdbc.driver.OracleDriver"),
    MYSQL("mysql", "com.mysql.jdbc.Driver"),
    MSSQL("mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver");

    private String name;
    private String className;

    DataBaseType(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }
}
