package com.code.hao.cache.interfaces;

import com.code.hao.cache.model.CacheObject;

import java.util.List;
import java.util.function.Supplier;

public interface CachePersist<K, V> extends Container {

    void setCosSupplier(Supplier<List<CacheObject<K, V>>> cosSupplier);

    void persist(List<CacheObject<K, V>> cos);

}
