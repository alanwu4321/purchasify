package com.dgs.v1;

import com.dgs.v1.model.ServiceLevelChart;
import com.dgs.v1.service.ForecastService;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.nio.charset.Charset;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@SpringBootApplication
@EnableAsync
public class V1Application {

    public static final Logger log = LoggerFactory.getLogger(V1Application.class);
    private static final Charset BIG5 = Charset.forName("BIG5");
    private static final Charset UTF8 = Charset.forName("UTF-8");


    public static void main(String[] args) throws SQLException {
        SpringApplication.run(V1Application.class, args);
    }

    @Bean
    public CommandLineRunner demo(ForecastService pd) {
        return (args) -> {
        };
    }
}
