package com.code.hao.cache.interfaces;

import java.util.Iterator;

public interface Cache<K, V> extends CacheBs<K, V> {

    int getCacheSize();

    long getCacheTimeout();

    V get(K key);

    void put(K key, V object);

    void put(K key, V object, long timeout);

    void remove(K key);

    int prune();

    void clear();

    Iterator<K> iterator();

    boolean isFull();

    int size();

    boolean isEmpty();
}
