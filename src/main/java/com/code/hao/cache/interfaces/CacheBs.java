package com.code.hao.cache.interfaces;

import org.springframework.context.ApplicationContext;

public interface CacheBs<K, V> {

    void setCacheLoader(CacheLoader<K, V> cacheLoader);

    void setCachePersist(CachePersist<K, V> cachePersist);

    void setRemoveCallback(RemoveCallback<K, V> removeCallback);

    void setContext(ApplicationContext context);

    void init();

    void destroy();
}
