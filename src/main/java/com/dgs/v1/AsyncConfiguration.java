package com.dgs.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class AsyncConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfiguration.class);

    public ThreadPoolTaskExecutor threadPoolTaskExecutor;


    public AsyncConfiguration() {
        //fetch autocomplete or forecast => 20 is enough
        this.threadPoolTaskExecutor = createPool(20);
    }

    public ThreadPoolTaskExecutor createPool(Integer coreSize) {
        LOGGER.info("Creating a new thread pool");
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(coreSize);
        threadPoolTaskExecutor.setMaxPoolSize(500);
        threadPoolTaskExecutor.setQueueCapacity(1500);
        threadPoolTaskExecutor.setThreadNamePrefix("sad-");
        threadPoolTaskExecutor.initialize();
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        return threadPoolTaskExecutor;
    }

    public long getCompletedTask() {
        return this.threadPoolTaskExecutor.getThreadPoolExecutor().getCompletedTaskCount();
    }


    public Boolean isShutdown() {
        return this.threadPoolTaskExecutor.getThreadPoolExecutor().isShutdown();
    }


    public void shutdown() {
        this.threadPoolTaskExecutor.getThreadPoolExecutor().shutdown();
    }


    public ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        return threadPoolTaskExecutor;
    }

    public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }
}