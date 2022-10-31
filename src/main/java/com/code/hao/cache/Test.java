package com.code.hao.cache;

import com.code.hao.cache.bs.CacheBs;
import com.code.hao.cache.core.FIFOCache;
import com.code.hao.cache.interfaces.Cache;
import com.code.hao.cache.interfaces.CacheLoader;
import com.code.hao.cache.model.CacheObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class Test {

    public static void main(String[] args) throws InterruptedException {

        String name = "Test";

        final CacheLoader<Integer, Integer> cacheLoader = new CacheLoader<Integer, Integer>() {
            @Override
            public Integer load(Integer key) {
                System.out.println("load num + 1 key=" + key);
                return key;
            }
            @Override
            public void loadAll(Map<Integer, CacheObject<Integer, Integer>> cacheMap) {
            }
        };

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        Cache<Integer, Integer> cache = CacheBs.<Integer, Integer>newInstance()
                .setName(name)
                // 设置 Cache 大小 （0代表不限制，默认是0）
                .setCacheSize(40)
                // 设置 Lock 是否公平 （默认是false）
                .setFair(false)
                // 设置 过期时间 （0代表永不过期，默认是0）
                .setTimeout(1000 * 20)
                // 设置 Load 加载
                .setCacheLoader(cacheLoader)
                // 设置 Persist 持久化
//                .setCachePersist(new RdbCachePersist<>(name, CacheConfig.PERSIST_PATH))
                // 设置 RemoveCallback 回调函数
//                .setRemoveCallback((key, value, removeReason) -> log.info("<<缓存回调>> 缓存被删除, key=" + key + ", value=" + value + ", reason=" + removeReason))
                // 设置 Spring Context，目的是为了事件监听，如果不想使用Spring或者有自造轮可自行进行修改
//                .setContext(context)
                // 构造参数传入具体的淘汰缓存类，该类需要实现 Cache<K, V> 接口
                // 并且提供有参构造函数(String.class, int.class, long.class, boolean.class)
                .build(FIFOCache.class);


        new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                cache.put(i, i, 1000 * 15);
            }
        }).start();

        new Thread(() -> {
            for (int i = 11; i <= 20; i++) {
                cache.put(i, i, 1000 * 15);
            }
        }).start();

        Thread.sleep(1000 * 4);

        new Thread(() -> {
            for (int i = 1; i <= 40; i++) {
                System.out.println(cache.get(i));
            }
        }).start();

        new Thread(() -> {
            for (int i = 1; i <= 40; i++) {
                System.out.println(cache.get(i));
            }
        }).start();

        new Thread(() -> {
            for (int i = 1; i <= 40; i++) {
                System.out.println(cache.get(i));
            }
        }).start();

    }
}
