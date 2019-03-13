package com.anluy.datapig.plugin.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库连接操作工具
 *
 * @author hc.zeng 2017-2-21
 * @version 1.0
 * @since Copyright &copy; 2016 <i>xinghuo.com</i>. All Rights Reserved
 */
public class DataBaseConnection {

    private DataBaseConnection() {

    }

    /**
     * 打开数据库连接
     *
     * @param dataBase      数据库类型
     * @param url      数据库连接URL
     * @param user     用户名
     * @param password 密码
     * @return 数据库连接
     * @throws ClassNotFoundException 找不到数据库驱动
     * @throws SQLException           数据库打开失败
     */
    public static Connection getConnection(DataBaseType dataBase,String url, String user, String password) throws ClassNotFoundException, SQLException {
        Class.forName(dataBase.getClassName());
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * 连接数据库，得到结果集
     *
     * @param con 数据库连接
     * @param sql 查询SQL
     * @return 查询结果
     * @throws SQLException 数据库操作异常
     */
    public static List<Object[]> getResultBySqlObject(Connection con, String sql) throws SQLException {

        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        List<Object[]> objectList = new ArrayList<>();
        while (rs.next()) {
            Object[] o = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                o[i] = rs.getString(i + 1);
            }
            objectList.add(o);
        }
        return objectList;
    }

    /**
     * 更新数据信息
     *
     * @param con  数据库连接
     * @param sql  更新的SQL
     * @param pram 更新的数据
     * @return 更新结果
     * @throws SQLException 数据库操作异常
     */
    public static int update(Connection con, String sql, Object[] pram) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        if (pram != null) {
            for (int i = 0; i < pram.length; i++) {
                ps.setObject(i + 1, pram[i]);
            }
        }
        int r = ps.executeUpdate();
        con.commit();
        return r;
    }

    /**
     * 批量更新数据信息
     *
     * @param con      数据库连接
     * @param sql      更新的SQL
     * @param pramList 更新的数据
     * @return 更新结果
     * @throws SQLException 数据库操作异常
     */
    public static int[] updateBatch(Connection con, String sql, List<Object[]> pramList) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        if (pramList != null && pramList.size() > 0) {
            for (Object[] pram : pramList) {
                for (int i = 0; i < pram.length; i++) {
                    ps.setObject(i + 1, pram[i]);
                }
                ps.addBatch();//加入批量处理
            }

        }
        int[] r = ps.executeBatch();//执行批量处理
        con.commit();
        return r;
    }
}
