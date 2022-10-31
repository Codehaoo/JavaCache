package com.code.hao.cache.core;

import com.code.hao.cache.model.CacheObject;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 返回有效的Key，在构造函数中利用深拷贝避免线程安全，但是弱一致性
 */
public class CacheValidKeysIterator<K, V> implements Iterator<K> {

    private final Iterator<? extends CacheObject<K, V>> iterator;
    private CacheObject<K, V> nextValue;

    CacheValidKeysIterator(List<CacheObject<K, V>> cacheObjects) {
        Objects.requireNonNull(cacheObjects);
        this.iterator = cacheObjects.iterator();
        nextValue();
    }

    private void nextValue() {
        while (iterator.hasNext()) {
            nextValue = iterator.next();
            // 这里不处理过期数据
            if (!nextValue.isExpired()) {
                return;
            }
        }
        nextValue = null;
    }

    public boolean hasNext() {
        return nextValue != null;
    }

    public K next() {
        K next = nextValue.key;
        nextValue();
        return next;
    }

    public void remove() {
        iterator.remove();
    }
}