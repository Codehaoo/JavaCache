package com.code.hao.cache.support.event;

import com.code.hao.cache.enums.RemoveReason;
import com.code.hao.cache.model.CacheObject;
import org.springframework.context.ApplicationEvent;

public class CacheRemoveEvent<K, V> extends ApplicationEvent {

    public final CacheObject<K, V> co;
    public final RemoveReason removeReason;

    public CacheRemoveEvent(Object source, CacheObject<K, V> co, RemoveReason removeReason) {
        super(source);
        this.co = co;
        this.removeReason = removeReason;
    }
}
