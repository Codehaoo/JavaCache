package com.code.hao.cache.core;

import com.code.hao.cache.enums.RemoveReason;
import com.code.hao.cache.model.CacheObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FIFOCache<K, V> extends AbstractCacheMap<K, V> {

    private final LinkedList<K> fifo = new LinkedList<>();

    private FIFOCache(String name, int cacheSize, long timeout, boolean fair) {
        super(name, cacheSize, timeout, fair);
    }

    @Override
    public int pruneCache() {
        int count = 0;
        Map<K, CacheObject<K, V>> cacheMap = this.cacheMap;
        Iterator<CacheObject<K, V>> values = cacheMap.values().iterator();
        while (values.hasNext()) {
            CacheObject<K, V> co = values.next();
            if (co.isExpired()) {
                values.remove();
                fifo.remove(co.key);
                count++;
                this.onRemove(co, RemoveReason.EXPIRED);
                this.onEvent(co, RemoveReason.EXPIRED);
                continue;
            }
        }
        if (cacheSize != 0 && cacheMap.size() >= cacheSize) {
            K first = fifo.getFirst();
            if (first != null) {
                CacheObject<K, V> co = cacheMap.remove(first);
                this.onRemove(co, RemoveReason.STRATEGY_WEED_OUT);
                this.onEvent(co, RemoveReason.STRATEGY_WEED_OUT);
                count++;
            }
        }
        return count;
    }

    @Override
    protected void whenAfterLoad(List<CacheObject<K, V>> cos) {
        for (CacheObject<K, V> co : cos) {
            fifo.addLast(co.key);
        }
    }

    @Override
    protected void whenAfterPut(K key, V object) {
        fifo.remove(key);
        fifo.addLast(key);
    }

    @Override
    protected void whenAfterRemove(K key) {
        fifo.remove(key);
    }

    @Override
    protected void whenAfterClear() {
        fifo.clear();
    }

    @Override
    protected void whenAfterClearSchedule(List<CacheObject<K, V>> cos) {
        for (CacheObject<K, V> co : cos) {
            fifo.remove(co.key);
        }
    }
}
