package com.code.hao.cache.support.persist;

import com.code.hao.cache.interfaces.CachePersist;
import com.code.hao.cache.model.CacheObject;

import java.util.List;
import java.util.function.Supplier;

public class NoneCachePersist<K, V> implements CachePersist<K, V> {

    @Override
    public void setCosSupplier(Supplier<List<CacheObject<K, V>>> cosSupplier) {
        // do nothing
    }

    @Override
    public void persist(List<CacheObject<K, V>> cos) {
        // do nothing
    }

    @Override
    public void start() {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }
}
