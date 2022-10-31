package com.code.hao.cache.bs;

import com.code.hao.cache.interfaces.Cache;
import com.code.hao.cache.interfaces.CacheLoader;
import com.code.hao.cache.interfaces.CachePersist;
import com.code.hao.cache.interfaces.RemoveCallback;
import com.code.hao.cache.support.load.NoneCacheLoad;
import com.code.hao.cache.support.persist.NoneCachePersist;
import com.code.hao.cache.support.removeCallback.NoneRemoveCallback;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class CacheBs<K, V> {

    private String name = new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "-Cache";
    private int cacheSize = 0;
    private long timeout = 0;
    private boolean fair = false;
    private CacheLoader<K, V> cacheLoader = new NoneCacheLoad<>();
    private CachePersist<K, V> cachePersist = new NoneCachePersist<>();
    private RemoveCallback<K, V> removeCallback = new NoneRemoveCallback<>();
    private ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

    private CacheBs() {
    }

    public static <K, V> CacheBs<K, V> newInstance() {
        return new CacheBs<>();
    }

    public CacheBs<K, V> setName(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }

    public CacheBs<K, V> setCacheSize(int cacheSize) {
        if (cacheSize < 0) {
            throw new RuntimeException("[Cache Building]:The cacheSize cannot be less than 0.");
        }
        this.cacheSize = cacheSize;
        return this;
    }

    public CacheBs<K, V> setTimeout(long timeout) {
        if (timeout < 0) {
            throw new RuntimeException("[Cache Building]:The timeout cannot be less than 0.");
        }
        this.timeout = timeout;
        return this;
    }

    public CacheBs<K, V> setFair(boolean fair) {
        this.fair = fair;
        return this;
    }

    public CacheBs<K, V> setCacheLoader(CacheLoader<K, V> cacheLoader) {
        Objects.requireNonNull(cacheLoader);
        this.cacheLoader = cacheLoader;
        return this;
    }

    public CacheBs<K, V> setCachePersist(CachePersist<K, V> cachePersist) {
        Objects.requireNonNull(cachePersist);
        this.cachePersist = cachePersist;
        return this;
    }

    public CacheBs<K, V> setRemoveCallback(RemoveCallback<K, V> removeCallback) {
        Objects.requireNonNull(removeCallback);
        this.removeCallback = removeCallback;
        return this;
    }

    public CacheBs<K, V> setContext(ApplicationContext context) {
        Objects.requireNonNull(context);
        this.context = context;
        return this;
    }

    /**
     * @param targetClazz 需要实现 Cache<K, V> 接口并且提供有参构造函数(String.class, int.class, long.class, boolean.class)
     * @return Cache<K, V>
     */
    public Cache<K, V> build(Class<? extends Cache> targetClazz) {
        try {
            Class<?> clazz = Class.forName(targetClazz.getName());
            Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, int.class, long.class, boolean.class);
            constructor.setAccessible(true);
            Cache<K, V> cache = (Cache<K, V>) constructor.newInstance(name, cacheSize, timeout, fair);
            cache.setCacheLoader(cacheLoader);
            cache.setCachePersist(cachePersist);
            cache.setRemoveCallback(removeCallback);
            cache.setContext(context);
            cache.init();
            return cache;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
