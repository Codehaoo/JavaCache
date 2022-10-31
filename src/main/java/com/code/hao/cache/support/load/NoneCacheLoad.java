package com.code.hao.cache.support.load;

import com.code.hao.cache.interfaces.CacheLoader;
import com.code.hao.cache.model.CacheObject;

import java.util.Map;

public class NoneCacheLoad<K, V> implements CacheLoader<K, V> {

    @Override
    public void loadAll(Map<K, CacheObject<K, V>> cacheMap) {
        // do nothing
    }

    @Override
    public V load(K key) {
        // do nothing
        return null;
    }
}
