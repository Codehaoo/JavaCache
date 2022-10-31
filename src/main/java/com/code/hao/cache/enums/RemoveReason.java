package com.code.hao.cache.enums;

public enum RemoveReason {

    /**
     * 过期
     */
    EXPIRED,
    /**
     * 定时清理
     */
    SCHEDULED_CLEAR,
    /**
     * 策略淘汰
     */
    STRATEGY_WEED_OUT,
    /**
     * 手动删除
     */
    MANUAL_REMOVE;

}