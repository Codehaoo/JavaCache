package com.code.hao.cache.support.healthy;

import com.code.hao.cache.config.CacheConfig;
import com.code.hao.cache.interfaces.Container;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@Slf4j
public class HealthyCheckSchedule implements Container {

    private final String name;
    private final Supplier<String> healthyInfoSupplier;
    private final ScheduledExecutorService EXECUTOR_SERVICE;

    public HealthyCheckSchedule(String name, Supplier<String> healthyInfoSupplier) {
        this.name = name;
        this.healthyInfoSupplier = healthyInfoSupplier;
        this.EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        log.info("<<{}>> healthy begin.", name);
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            try {
                String healthyInfo = healthyInfoSupplier.get();
                log.info("<<{}>> healthy checking. healthyInfo={}", name, healthyInfo);
            } catch (Exception exception) {
                log.error("<<{}>> healthy error. exception={}", name, exception);
            }
        }, CacheConfig.HEALTHY_DELAY, CacheConfig.HEALTHY_PERIOD, CacheConfig.HEALTHY_TIME_UNIT);
    }

    @Override
    public void close() {
        try {
            EXECUTOR_SERVICE.shutdown();
        } catch (Throwable t) {
            log.error("<<{}>> healthy shutdown error. exception={}", name, t);
        }
    }
}
