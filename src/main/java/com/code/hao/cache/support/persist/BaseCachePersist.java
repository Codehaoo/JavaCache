package com.code.hao.cache.support.persist;

import com.code.hao.cache.config.CacheConfig;
import com.code.hao.cache.interfaces.CachePersist;
import com.code.hao.cache.model.CacheObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@Slf4j
public abstract class BaseCachePersist<K, V> implements CachePersist<K, V> {

    private final String name;
    private final ScheduledExecutorService EXECUTOR_SERVICE;
    private Supplier<List<CacheObject<K, V>>> cosSupplier;

    public BaseCachePersist(String name) {
        this.name = name;
        this.EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void setCosSupplier(Supplier<List<CacheObject<K, V>>> cosSupplier) {
        this.cosSupplier = cosSupplier;
    }

    @Override
    public void start() {
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                long begin = System.currentTimeMillis();
                log.info("<<{}>> cache persist begin.", name);
                List<CacheObject<K, V>> cos = cosSupplier.get();
                persist(cos);
                log.info("<<{}>> cache persist end. costTime={}(ms)", name, System.currentTimeMillis() - begin);
            } catch (Exception exception) {
                log.error("<<{}>> cache persist error. exception={}", name, exception);
            }
        }, CacheConfig.PERSIST_DELAY, CacheConfig.PERSIST_PERIOD, CacheConfig.PERSIST_TIME_UNIT);
    }

    @Override
    public void close() {
        try {
            EXECUTOR_SERVICE.shutdown();
        } catch (Throwable t) {
            log.error("<<{}>> cache persist shutdown error. exception={}", name, t);
        }
    }
}
