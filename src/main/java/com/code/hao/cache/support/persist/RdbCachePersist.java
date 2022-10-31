package com.code.hao.cache.support.persist;

import com.code.hao.cache.model.CacheObject;
import com.code.hao.cache.utils.FileUtil;

import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RdbCachePersist<K, V> extends BaseCachePersist<K, V> {

    private final String path;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public RdbCachePersist(String name, String path) {
        super(name);
        this.path = path;
    }

    @Override
    public void persist(List<CacheObject<K, V>> cos) {
        String format = this.sdf.format(new Date());
        String filePath = this.path + format + ".rdb";
        // 创建文件
        FileUtil.createFile(filePath);
        // 清空文件
        FileUtil.truncate(filePath);
        for (CacheObject<K, V> co : cos) {
            String line = co.toString();
            FileUtil.write(filePath, line, StandardOpenOption.APPEND);
        }
    }
}
