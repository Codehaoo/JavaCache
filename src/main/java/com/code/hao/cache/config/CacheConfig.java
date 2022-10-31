package com.code.hao.cache.config;

import java.util.concurrent.TimeUnit;

public class CacheConfig {

    // ======================== 加载数据 ========================
    /**
     * 是否开启自动加载数据
     */
    public static final boolean AUTO_LOAD_DATA = true;


    // ======================== 心跳机制 ========================
    /**
     * 是否开启心跳机制
     */
    public static final boolean HEALTHY_CHECK = true;
    /**
     * 心跳机制延迟执行时间
     */
    public static final long HEALTHY_DELAY = 5;
    /**
     * 心跳机制执行周期
     */
    public static final long HEALTHY_PERIOD = 5;
    /**
     * 心跳机制时间单位
     */
    public static final TimeUnit HEALTHY_TIME_UNIT = TimeUnit.SECONDS;


    // ======================== 定时删除 ========================
    /**
     * 是否开启定时删除
     */
    public static final boolean SCHEDULED_CLEAR = true;
    /**
     * 定时删除延迟执行时间
     */
    public static final long CLEAR_DELAY = 10;
    /**
     * 定时删除执行周期
     */
    public static final long CLEAR_PERIOD = 10;
    /**
     * 定时删除时间单位
     */
    public static final TimeUnit CLEAR_TIME_UNIT = TimeUnit.SECONDS;


    // ======================== 持久化 ========================
    /**
     * 是否开启定时持久化
     */
    public static final boolean AUTO_PERSIST = true;
    /**
     * 定时持久化延迟执行时间
     */
    public static final long PERSIST_DELAY = 10;
    /**
     * 定时持久化执行周期
     */
    public static final long PERSIST_PERIOD = 10;
    /**
     * 定时持久化时间单位
     */
    public static final TimeUnit PERSIST_TIME_UNIT = TimeUnit.SECONDS;
    /**
     * 定时持久化文件存放位置
     */
    public static final String PERSIST_PATH = "C:/Users/Administrator/Desktop/cache/src/main/resources/";

}
