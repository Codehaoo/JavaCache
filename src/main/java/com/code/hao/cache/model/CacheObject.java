package com.code.hao.cache.model;

public class CacheObject<K, V> {

    public final K key;
    public final V cacheObject;
    public final long ttl;
    public long lastAccess;
    public long accessCount;

    public CacheObject(K key, V object, long ttl) {
        this.key = key;
        this.cacheObject = object;
        this.ttl = ttl;
        this.lastAccess = System.currentTimeMillis();
    }

    public boolean isExpired() {
        if (ttl == 0) {
            return false;
        }
        return lastAccess + ttl < System.currentTimeMillis();
    }

    public V getObject() {
        lastAccess = System.currentTimeMillis();
        accessCount++;
        return cacheObject;
    }


    @Override
    public String toString() {
        return "CacheObject{" +
                "key=" + key +
                ", cacheObject=" + cacheObject +
                ", lastAccess=" + lastAccess +
                ", accessCount=" + accessCount +
                ", ttl=" + ttl +
                '}';
    }
}
