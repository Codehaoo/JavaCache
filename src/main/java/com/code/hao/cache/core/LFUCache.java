package com.code.hao.cache.core;

import com.code.hao.cache.enums.RemoveReason;
import com.code.hao.cache.model.CacheObject;

import java.util.Iterator;
import java.util.Map;

public class LFUCache<K, V> extends AbstractCacheMap<K, V> {

    private LFUCache(String name, int cacheSize, long timeout, boolean fair) {
        super(name, cacheSize, timeout, fair);
    }

    @Override
    public int pruneCache() {
        int count = 0;
        CacheObject<K, V> result = null;
        Map<K, CacheObject<K, V>> cacheMap = this.cacheMap;
        Iterator<CacheObject<K, V>> values = cacheMap.values().iterator();
        while (values.hasNext()) {
            CacheObject<K, V> co = values.next();
            if (co.isExpired()) {
                values.remove();
                count++;
                this.onRemove(co, RemoveReason.EXPIRED);
                this.onEvent(co, RemoveReason.EXPIRED);
                continue;
            }
            if (result == null) {
                result = co;
            } else {
                if (co.accessCount < result.accessCount) {
                    result = co;
                }
            }
        }
        if (cacheSize != 0 && cacheMap.size() >= cacheSize) {
            if (result != null) {
                long minAccessCount = result.accessCount;
                values = cacheMap.values().iterator();
                while (values.hasNext()) {
                    CacheObject<K, V> co = values.next();
                    co.accessCount -= minAccessCount;
                    if (co.accessCount <= 0) {
                        values.remove();
                        count++;
                        this.onRemove(co, RemoveReason.STRATEGY_WEED_OUT);
                        this.onEvent(co, RemoveReason.STRATEGY_WEED_OUT);
                    }
                }
            }
        }
        return count;
    }
}
