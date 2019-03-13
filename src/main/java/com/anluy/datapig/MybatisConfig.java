package com.anluy.datapig;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Database配置信息
 *
 * @author hc.zeng
 * @create 2018-10-19 14:39
 */
@Configuration
public class MybatisConfig {

    private static final Logger LOG = LogManager.getLogger(MybatisConfig.class);

    @Autowired
    public Environment env;

    protected DruidDataSource getDruidDataSource(Environment env, String url, String username, String password) {
        try {
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setUrl(url);
            druidDataSource.setUsername(username);
            druidDataSource.setPassword(password);

            int timeBetweenEvictionRunsMillis = Integer.valueOf(env.getProperty("druid.timeBetweenEvictionRunsMillis", "3000"));
            druidDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

            int minEvictableIdleTimeMillis = Integer.valueOf(env.getProperty("druid.minEvictableIdleTimeMillis", "300000"));
            druidDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);

            String validationQuery = env.getProperty("druid.validationQuery", "SELECT 'C' FROM DUAL");
            druidDataSource.setValidationQuery(validationQuery);

            boolean testWhileIdle = Boolean.valueOf(env.getProperty("druid.testWhileIdle", "true"));
            druidDataSource.setTestWhileIdle(testWhileIdle);

            boolean testOnBorrow = Boolean.valueOf(env.getProperty("druid.testOnBorrow", "false"));
            druidDataSource.setTestOnBorrow(testOnBorrow);

            boolean testOnReturn = Boolean.valueOf(env.getProperty("druid.testOnReturn", "false"));
            druidDataSource.setTestOnReturn(testOnReturn);

            boolean poolPreparedStatements = Boolean.valueOf(env.getProperty("druid.poolPreparedStatements", "true"));
            druidDataSource.setPoolPreparedStatements(poolPreparedStatements);

            int maxActive = Integer.valueOf(env.getProperty("druid.maxActive", "10"));
            druidDataSource.setMaxActive(maxActive);

            int maxPoolPreparedStatementPerConnectionSize = Integer.valueOf(env.getProperty("druid.maxPoolPreparedStatementPerConnectionSize", "50"));
            druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
            return druidDataSource;
        } catch (Exception e) {
            LOG.error("初始化数据源异常！", e);
            throw new RuntimeException(e);
        }
    }


    @Bean("dataSource")
    @Primary
    public DataSource dataSource() {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        return getDruidDataSource(env, url, username, password);
    }

    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("/mybatis-config.xml"));
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:mapper/**/*Dao.xml");
            for (Resource resource : resources) {
                LOG.info(resource.getFilename() + " - " + resource.getURI().toString());
            }
            sqlSessionFactoryBean.setMapperLocations(resources);
            return sqlSessionFactoryBean.getObject();
        } catch (Exception e) {
            LOG.error("Mybatis初始化SqlSessionFactory异常!", e);
            throw new RuntimeException(e);
        }
    }

    @Bean("transactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("jdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
