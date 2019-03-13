package com.anluy.datapig.dao;

import com.anluy.datapig.dao.dialect.Dialect;
import com.anluy.datapig.dao.dialect.MySql5Dialect;
import com.anluy.datapig.dao.dialect.OracleDialect;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.Properties;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-20 18:55
 */

@Intercepts({@Signature(
        type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class, Integer.class}
)})
public class PaginationInterceptor implements Interceptor {
    private static final Logger log = LogManager.getLogger(PaginationInterceptor.class);

    public PaginationInterceptor() {
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        RowBounds rowBounds = (RowBounds) metaStatementHandler.getValue("delegate.rowBounds");
        String originalSql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
        if (log.isDebugEnabled()) {
            log.debug("SQL : " + originalSql);
        }

        if (rowBounds != null && rowBounds != RowBounds.DEFAULT) {
            Configuration configuration = (Configuration) metaStatementHandler.getValue("delegate.configuration");
            Dialect.Type databaseType = null;

            try {
                databaseType = Dialect.Type.valueOf(configuration.getVariables().getProperty("dialect").toUpperCase());
            } catch (Exception var10) {
                ;
            }

            if (databaseType == null) {
                throw new RuntimeException("the value of the dialect property in configuration.xml is not defined : " + configuration.getVariables().getProperty("dialect"));
            } else {
                Object dialect = null;
                switch (databaseType) {
                    case ORACLE:
                        dialect = new OracleDialect();
                        break;
                    case MYSQL:
                        dialect = new MySql5Dialect();
                }

                metaStatementHandler.setValue("delegate.boundSql.sql", ((Dialect) dialect).getLimitString(originalSql, rowBounds.getOffset(), rowBounds.getLimit()));
                metaStatementHandler.setValue("delegate.rowBounds.offset", Integer.valueOf(0));
                metaStatementHandler.setValue("delegate.rowBounds.limit", Integer.MAX_VALUE);
                if (log.isDebugEnabled()) {
                    log.debug("生成分页SQL : " + boundSql.getSql());
                }
                return invocation.proceed();
            }
        } else {
            return invocation.proceed();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
