package com.code.hao.cache.support.event.listener;

import com.code.hao.cache.model.CacheObject;
import com.code.hao.cache.support.event.CacheRemoveEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheRemoveEventListener<K, V> {

    @EventListener
    public void onApplicationEvent(CacheRemoveEvent<K, V> event) {
        CacheObject<K, V> co = event.co;
        log.info("<<缓存监听>> 缓存被删除, key=" + co.key + ", value=" + co.cacheObject + ", reason=" + event.removeReason);
    }
}
