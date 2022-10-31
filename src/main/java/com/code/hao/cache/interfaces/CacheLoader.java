package com.code.hao.cache.interfaces;

import com.code.hao.cache.model.CacheObject;

import java.util.Map;

public interface CacheLoader<K, V> {

    void loadAll(Map<K, CacheObject<K, V>> cacheMap);

    V load(K key);

}
