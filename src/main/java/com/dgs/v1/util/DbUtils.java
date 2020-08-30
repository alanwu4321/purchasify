package com.dgs.v1.util;

import com.dgs.v1.service.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Component
public class DbUtils {
    public static final Charset BIG5 = Charset.forName("BIG5");
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtils.class);

    public static Double getDouble(Object obj) {
        try {
            BigDecimal d = (BigDecimal) obj;
            return d.doubleValue();
        } catch (ClassCastException e) {
            return (Double) obj;
        }
    }

    //default to cacheable true if not specified
    public static List<HashMap<String, Object>> query(Context ctx, String query) {
        return query(ctx, query, true);
    }


    public static List<HashMap<String, Object>> query(Context ctx, String query, Boolean cacheable) {
        if(ctx.isShowQuery()) LOGGER.info(query);
        List<HashMap<String, Object>> list = new ArrayList<>();
        Gson gson = new Gson();
        Jedis jedis = ctx.getJedisPool().getResource();
        String hashedKey = String.format("%s:spring:%d", ctx.getCompany(), query.hashCode());
        try {
            //skip this if refresh cache is set to true or not cachable
            if (cacheable && !ctx.isRefreshCache() && jedis.exists(hashedKey)) {

                String json = jedis.get(hashedKey);
                Long ttl = jedis.ttl(hashedKey);
                if(ctx.isShowCache()) {
                    LOGGER.info("Cache HIT!");
                    LOGGER.info("Key => " + hashedKey);
                    LOGGER.info("TTL => " + ttl);
                }
                list = gson.fromJson(json, new TypeToken<List<HashMap<String, Object>>>() {
                }.getType());
                return list;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            try {
                if (jedis != null)
                    jedis.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        if(ctx.isShowCache()) {
            LOGGER.info("Cache MISS!\n Fetching from database");
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = ctx.getDbPool().getConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<>(columns);
                for (int i = 1; i <= columns; ++i) {
                    Object object = null;
                    try {
                        object = rs.getObject(md.getColumnName(i));
                    } catch (SQLException e) {
                        // Chinese character needs BIG5 conversion from SQL_ASCII Database
                        byte[] bytes = rs.getBytes(md.getColumnName(i));
                        object = new String(bytes, BIG5);
                    }
                    row.put(md.getColumnName(i), object);
                }
                list.add(row);
            }
            //only set cache if the query is cacheable
            if(cacheable) {
                jedis = ctx.getJedisPool().getResource();
                if(ctx.isShowCache()) {
                    LOGGER.warn("Updating cache for query");
                    LOGGER.warn("Key =>" + hashedKey);
                }
                Transaction t = jedis.multi();
                t.set(hashedKey, gson.toJson(list));
                t.expire(hashedKey, (int) ctx.getExpirary());
                t.exec();
            }
            return list;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        } finally {
            //In JedisPool mode, the Jedis resource will be returned to the resource pool.
            try {
                if (jedis != null)
                    jedis.close();
            } catch (Exception e) {
                LOGGER.error("Jedis close" + e.getMessage());
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                LOGGER.error("PG conn" + e.getMessage());
            }
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            try {
                if (ps != null)
                    ps.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

}
