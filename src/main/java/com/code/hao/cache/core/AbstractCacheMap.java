package com.code.hao.cache.core;

import com.code.hao.cache.config.CacheConfig;
import com.code.hao.cache.enums.RemoveReason;
import com.code.hao.cache.interfaces.Cache;
import com.code.hao.cache.interfaces.CacheLoader;
import com.code.hao.cache.interfaces.CachePersist;
import com.code.hao.cache.interfaces.Container;
import com.code.hao.cache.interfaces.RemoveCallback;
import com.code.hao.cache.model.CacheObject;
import com.code.hao.cache.support.clear.CacheScheduleClear;
import com.code.hao.cache.support.event.CacheRemoveEvent;
import com.code.hao.cache.support.healthy.HealthyCheckSchedule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractCacheMap<K, V> implements Cache<K, V> {

    // base
    protected final String name;
    protected final int cacheSize; // 0 == no limit
    protected final long timeout; // 0 == no limit
    protected final Map<K, Future<V>> futureTaskMap;
    private final ReentrantReadWriteLock lock;
    // 心跳
    private final Container healthyCheck;
    // 定时删除
    private final Container cacheClear;
    // stat
    protected int hitCount;
    protected int missCount;
    // cache
    protected Map<K, CacheObject<K, V>> cacheMap;
    // load
    private CacheLoader<K, V> cacheLoader;
    // persist
    private CachePersist<K, V> cachePersist;
    // callback
    private RemoveCallback<K, V> removeCallback;
    // event
    private ApplicationContext context;

    public AbstractCacheMap(String name, int cacheSize, long timeout, boolean fair) {
        this.name = name;
        this.cacheSize = cacheSize;
        this.timeout = timeout;
        this.lock = new ReentrantReadWriteLock(fair);
        this.cacheMap = new ConcurrentHashMap<>(cacheSize + 1);
        this.futureTaskMap = new ConcurrentHashMap<>(cacheSize + 1);
        if (CacheConfig.HEALTHY_CHECK) {
            this.healthyCheck = new HealthyCheckSchedule(name, this::toString);
        }
        if (CacheConfig.SCHEDULED_CLEAR) {
            this.cacheClear = new CacheScheduleClear(name, this::clearExpiredSchedule);
        }
    }

    @Override
    public void setCacheLoader(CacheLoader<K, V> cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    @Override
    public void setCachePersist(CachePersist<K, V> cachePersist) {
        cachePersist.setCosSupplier(() -> {
            ReentrantReadWriteLock lock = this.lock;
            lock.readLock().lock();
            try {
                return new ArrayList<>(this.cacheMap.values());
            } finally {
                lock.readLock().unlock();
            }
        });
        this.cachePersist = cachePersist;
    }

    @Override
    public void setRemoveCallback(RemoveCallback<K, V> removeCallback) {
        this.removeCallback = removeCallback;
    }

    @Override
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void init() {
        ReentrantReadWriteLock lock = this.lock;
        if (CacheConfig.AUTO_LOAD_DATA) {
            lock.writeLock().lock();
            try {
                this.cacheLoader.loadAll(cacheMap);
                whenAfterLoad(new ArrayList<>(cacheMap.values()));
            } finally {
                lock.writeLock().unlock();
            }
        }
        if (CacheConfig.AUTO_PERSIST) {
            this.cachePersist.start();
        }
        if (CacheConfig.HEALTHY_CHECK) {
            this.healthyCheck.start();
        }
        if (CacheConfig.SCHEDULED_CLEAR) {
            this.cacheClear.start();
        }
    }

    protected void whenAfterLoad(List<CacheObject<K, V>> cos) {
    }

    @Override
    public void destroy() {
        if (CacheConfig.AUTO_PERSIST) {
            this.cachePersist.close();
        }
        if (CacheConfig.HEALTHY_CHECK) {
            this.healthyCheck.close();
        }
        if (CacheConfig.SCHEDULED_CLEAR) {
            this.cacheClear.close();
        }
    }

    protected void onRemove(CacheObject<K, V> cacheObject, RemoveReason removeReason) {
        RemoveCallback<K, V> removeCallback = this.removeCallback;
        if (removeCallback != null && cacheObject != null) {
            try {
                removeCallback.onRemove(cacheObject.key, cacheObject.getObject(), removeReason);
            } catch (Exception ex) {
                //do nothing
            }
        }
    }

    protected void onEvent(CacheObject<K, V> cacheObject, RemoveReason removeReason) {
        ApplicationEventPublisher context = this.context;
        if (cacheObject != null) {
            try {
                context.publishEvent(new CacheRemoveEvent<>(this, cacheObject, removeReason));
            } catch (Exception ex) {
                //do nothing
            }
        }
    }

    @Override
    public int getCacheSize() {
        return cacheSize;
    }

    @Override
    public long getCacheTimeout() {
        return timeout;
    }

    @Override
    public V get(K key) {
        Map<K, CacheObject<K, V>> cacheMap = this.cacheMap;
        Map<K, Future<V>> futureTaskMap = this.futureTaskMap;
        ReentrantReadWriteLock lock = this.lock;
        lock.readLock().lock();
        try {
            CacheObject<K, V> co = cacheMap.get(key);
            if (co == null) {
                missCount++;
                // 保证 load 只执行一次
                Future<V> f = futureTaskMap.get(key);
                if (f == null) {
                    FutureTask<V> ft = new FutureTask<>(() -> {
                        V value = load(key);
                        if (value == null) {
                            return null;
                        } else {
                            cacheMap.put(key, new CacheObject<>(key, value, timeout));
                            return value;
                        }
                    });
                    f = futureTaskMap.putIfAbsent(key, ft);
                    if (f == null) {
                        f = ft;
                        ft.run();
                        futureTaskMap.remove(key);
                    }
                }
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (co.isExpired()) {
                missCount++;
                return null;
            }
            hitCount++;
            return co.getObject();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void put(K key, V object) {
        put(key, object, timeout);
    }

    public void put(K key, V object, long timeout) {
        CacheObject<K, V> co = new CacheObject<>(key, object, timeout);
        ReentrantReadWriteLock lock = this.lock;
        lock.writeLock().lock();
        try {
            if (cacheSize != 0 && cacheMap.size() >= cacheSize) {
                pruneCache();
            }
            this.cacheMap.put(key, co);
            whenAfterPut(key, object);
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected void whenAfterPut(K key, V object) {
    }

    @Override
    public void remove(K key) {
        CacheObject<K, V> co;
        ReentrantReadWriteLock lock = this.lock;
        lock.writeLock().lock();
        try {
            co = this.cacheMap.remove(key);
            whenAfterRemove(key);
        } finally {
            lock.writeLock().unlock();
        }
        this.onRemove(co, RemoveReason.MANUAL_REMOVE);
        this.onEvent(co, RemoveReason.MANUAL_REMOVE);
    }

    protected void whenAfterRemove(K key) {
    }

    @Override
    public int prune() {
        int count;
        ReentrantReadWriteLock lock = this.lock;
        lock.writeLock().lock();
        try {
            count = pruneCache();
        } finally {
            lock.writeLock().unlock();
        }
        return count;
    }

    protected abstract int pruneCache();

    @Override
    public void clear() {
        List<CacheObject<K, V>> cos;
        Map<K, CacheObject<K, V>> cacheMap = this.cacheMap;
        ReentrantReadWriteLock lock = this.lock;
        lock.writeLock().lock();
        try {
            cos = new ArrayList<>(cacheMap.values());
            cacheMap.clear();
            this.futureTaskMap.clear();
            whenAfterClear();
        } finally {
            lock.writeLock().unlock();
        }
        for (CacheObject<K, V> co : cos) {
            this.onRemove(co, RemoveReason.MANUAL_REMOVE);
            this.onEvent(co, RemoveReason.MANUAL_REMOVE);
        }
    }

    protected void whenAfterClear() {
    }

    private void clearExpiredSchedule() {
        List<CacheObject<K, V>> cos = new ArrayList<>(cacheMap.size());
        Map<K, CacheObject<K, V>> cacheMap = this.cacheMap;
        ReentrantReadWriteLock lock = this.lock;
        lock.writeLock().lock();
        try {
            Iterator<CacheObject<K, V>> iterator = cacheMap.values().iterator();
            while (iterator.hasNext()) {
                CacheObject<K, V> co = iterator.next();
                if (co.isExpired()) {
                    iterator.remove();
                    cos.add(co);
                }
            }
            this.futureTaskMap.clear();
            whenAfterClearSchedule(cos);
        } finally {
            lock.writeLock().unlock();
        }
        for (CacheObject<K, V> co : cos) {
            this.onRemove(co, RemoveReason.SCHEDULED_CLEAR);
            this.onEvent(co, RemoveReason.SCHEDULED_CLEAR);
        }
    }

    protected void whenAfterClearSchedule(List<CacheObject<K, V>> cos) {
    }

    private V load(K key) {
        CacheLoader<K, V> cacheLoader = this.cacheLoader;
        return CacheConfig.AUTO_LOAD_DATA && cacheLoader != null ? cacheLoader.load(key) : null;
    }

    @Override
    public Iterator<K> iterator() {
        List<CacheObject<K, V>> cos;
        ReentrantReadWriteLock lock = this.lock;
        lock.readLock().lock();
        try {
            cos = new ArrayList<>(this.cacheMap.values());
        } finally {
            lock.readLock().unlock();
        }
        return new CacheValidKeysIterator<>(cos);
    }

    @Override
    public boolean isFull() {
        if (cacheSize == 0) {
            return false;
        }
        return size() >= cacheSize;
    }

    @Override
    public int size() {
        ReentrantReadWriteLock lock = this.lock;
        lock.readLock().lock();
        try {
            return cacheMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getMissCount() {
        return missCount;
    }

    @Override
    public String toString() {
        return "AbstractCacheMap{" +
                "name='" + name + '\'' +
                ", totalCacheSize=" + cacheSize +
                ", curCacheSize=" + size() +
                ", timeout=" + timeout +
                ", hitCount=" + hitCount +
                ", missCount=" + missCount +
                ", hitRate=" + (double) hitCount / (hitCount + missCount) +
                '}';
    }

}
