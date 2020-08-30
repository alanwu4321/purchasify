package com.dgs.v1.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.dgs.v1.DataSourcePool;
import com.dgs.v1.DbPoolConfiguration;
import com.dgs.v1.JedisPoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@WebFilter("/*")
public class StatsFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsFilter.class);

    @Autowired
    private DbPoolConfiguration dc;

    @Autowired
    private JedisPoolConfiguration jc;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // empty
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        long time = System.currentTimeMillis();
        try {
            chain.doFilter(req, resp);
        } finally {
            time = System.currentTimeMillis() - time;
            LOGGER.info("{}{}: {} ms ", ((HttpServletRequest) req).getRequestURI(), ((HttpServletRequest) req).getQueryString(), time);
//            healthCheck(jc, dc);
        }
    }

    public static void healthCheck(DataSourcePool ...pools) {
        Arrays.stream(pools).forEach(pool -> pool.healthCheck());
    }

    @Override
    public void destroy() {
        // empty
    }
}
