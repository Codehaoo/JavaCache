package com.code.hao.cache.support.clear;

import com.code.hao.cache.config.CacheConfig;
import com.code.hao.cache.interfaces.CacheClear;
import com.code.hao.cache.interfaces.Container;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class CacheScheduleClear implements Container {

    private final String name;
    private final CacheClear cacheClear;
    private final ScheduledExecutorService EXECUTOR_SERVICE;

    public CacheScheduleClear(String name, CacheClear cacheClear) {
        this.name = name;
        this.cacheClear = cacheClear;
        this.EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                long begin = System.currentTimeMillis();
                log.info("<<{}>> cache clear begin.", name);
                this.cacheClear.todo();
                log.info("<<{}>> cache clear end. costTime={}(ms)", name, System.currentTimeMillis() - begin);
            } catch (Exception exception) {
                log.error("<<{}>> cache clear error. exception={}", name, exception);
            }
        }, CacheConfig.CLEAR_DELAY, CacheConfig.CLEAR_PERIOD, CacheConfig.CLEAR_TIME_UNIT);
    }

    @Override
    public void close() {
        try {
            EXECUTOR_SERVICE.shutdown();
        } catch (Throwable t) {
            log.error("<<{}>> cache clear shutdown error. exception={}", name, t);
        }
    }
}
