package com.dgs.v1;

import com.dgs.v1.controller.CarController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Component
public class JedisPoolConfiguration implements DataSourcePool<JedisPool, Jedis>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisPoolConfiguration.class);


    private static JedisPoolConfig poolConfig = buildPoolConfig();
    private static JedisPool jedisPool = new JedisPool(poolConfig, "localhost",6379, 10000);

    private static JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        //one task will require 5 connections
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(15);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    @Override
    public JedisPool getPool() {
        return jedisPool;
    }

    @Override
    public Jedis getConnection() {
        return jedisPool.getResource();
    }

    @Override
    public void healthCheck() {
        LOGGER.info("Redis Active "  + jedisPool.getNumActive());
        LOGGER.info("Redis Idle "  + jedisPool.getNumIdle());
        LOGGER.info("Redis Waiting "  + jedisPool.getNumWaiters());
    }

//
//    public Integer setConnection() {
//        return cpds++;
//    }
}
