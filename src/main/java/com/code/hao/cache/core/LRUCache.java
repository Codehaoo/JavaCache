package com.code.hao.cache.core;

import com.code.hao.cache.enums.RemoveReason;
import com.code.hao.cache.model.CacheObject;

import java.util.Iterator;

public class LRUCache<K, V> extends AbstractCacheMap<K, V> {

    private LRUCache(String name, int cacheSize, long timeout, boolean fair) {
        super(name, cacheSize, timeout, fair);
    }

    @Override
    public int pruneCache() {
        int count = 0;
        CacheObject<K, V> result = null;
        Iterator<CacheObject<K, V>> values = cacheMap.values().iterator();
        while (values.hasNext()) {
            CacheObject<K, V> co = values.next();
            if (co.isExpired()) {
                values.remove();
                count++;
                this.onRemove(co, RemoveReason.EXPIRED);
                this.onEvent(co, RemoveReason.EXPIRED);
            }
            if (result == null) {
                result = co;
            } else {
                if (co.lastAccess < result.lastAccess) {
                    result = co;
                }
            }
        }
        if (cacheSize != 0 && cacheMap.size() >= cacheSize) {
            if (result != null) {
                long minLastAccess = result.lastAccess;
                values = cacheMap.values().iterator();
                while (values.hasNext()) {
                    CacheObject<K, V> co = values.next();
                    if (co.lastAccess <= minLastAccess) {
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
