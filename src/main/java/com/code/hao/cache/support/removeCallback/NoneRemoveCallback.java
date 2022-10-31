package com.code.hao.cache.support.removeCallback;

import com.code.hao.cache.enums.RemoveReason;
import com.code.hao.cache.interfaces.RemoveCallback;

public class NoneRemoveCallback<K, V> implements RemoveCallback<K, V> {

    @Override
    public void onRemove(K k, V v, RemoveReason removeReason) {
        // do nothing
    }
}
