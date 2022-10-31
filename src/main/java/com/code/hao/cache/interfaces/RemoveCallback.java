package com.code.hao.cache.interfaces;

import com.code.hao.cache.enums.RemoveReason;

public interface RemoveCallback<K, V> {

    void onRemove(K k, V v, RemoveReason removeReason);

}