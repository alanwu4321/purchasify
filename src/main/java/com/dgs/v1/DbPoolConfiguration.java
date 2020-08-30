package com.dgs.v1;

import com.dgs.v1.service.Context;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DbPoolConfiguration implements DataSourcePool<ComboPooledDataSource, Connection> {

    public static final String TAIWAN = "TAIWAN";
    private static final Logger LOGGER = LoggerFactory.getLogger(DbPoolConfiguration.class);
    private static final String CHINA = "CHINA";

    private static ComboPooledDataSource cpds = new ComboPooledDataSource();

    //TODO MAKE IT NON STATIC
    @Value("${spring.datasource.url}")
    private static String url;
    @Value("${spring.datasource.username}")
    private static String user;
    @Value("${spring.datasource.password}")
    private static String password;

   {
        try {
            cpds.setJdbcUrl(url+ TAIWAN);
            cpds.setUser(user);
            cpds.setPassword(password);
            // Optional Settings
            cpds.setInitialPoolSize(2);
            cpds.setMinPoolSize(1);
            cpds.setAcquireIncrement(20);
            cpds.setMaxPoolSize(20);
            cpds.setMaxStatements(88);
            //after a minute start reaping idle connection
            cpds.setMaxIdleTime(60);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            // handle the exception
        }
    }

    @Override
    public ComboPooledDataSource getPool() {
        return cpds;
    }

    public ComboPooledDataSource getPool(Context ctx) {
        String dbname = TAIWAN;
        if (ctx.getCompany().equals(Context.CHINA))
            dbname = CHINA;
        cpds.setJdbcUrl("jdbc:postgresql://192.168.0.200:5432/" + dbname);
        return cpds;
    }

    @Override
    public Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void healthCheck() {
        try {
            LOGGER.info("DB Active Conn " + cpds.getNumConnections());
            LOGGER.info("DB Idle Conn " + cpds.getNumIdleConnections());
            LOGGER.info("DB Thread Pool Active " + cpds.getThreadPoolSize());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
