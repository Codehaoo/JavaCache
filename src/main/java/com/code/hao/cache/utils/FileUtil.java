package com.code.hao.cache.utils;

import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Objects;

/**
 * copy from bbhou
 */
public class FileUtil {

    public static void createFile(final String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
        if (FileUtil.exists(filePath)) {
            return;
        }
        File file = new File(filePath);
        // 父类文件夹的处理
        File dir = file.getParentFile();
        if (dir != null && FileUtil.notExists(dir)) {
            boolean mkdirResult = dir.mkdirs();
            if (!mkdirResult) {
                return;
            }
        }
        // 创建文件
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean exists(final String filePath, LinkOption... options) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }

        Path path = Paths.get(filePath);
        return Files.exists(path, options);
    }

    public static boolean notExists(final File file) {
        Objects.requireNonNull(file);
        return !file.exists();
    }

    public static void truncate(final String filePath) {
        FileUtil.write(filePath, "", StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void write(final String filePath, final CharSequence line, OpenOption... openOptions) {
        write(filePath, Collections.singletonList(line), openOptions);
    }

    public static void write(final String filePath, final Iterable<? extends CharSequence> lines, OpenOption... openOptions) {
        write(filePath, lines, "UTF-8", openOptions);
    }

    public static void write(final String filePath, final Iterable<? extends CharSequence> lines,
                             final String charset, OpenOption... openOptions) {
        try {
            Objects.requireNonNull(lines);
            CharsetEncoder encoder = Charset.forName(charset).newEncoder();
            final Path path = Paths.get(filePath);

            // 创建父类文件夹
            Path pathParent = path.getParent();
            // 路径判断空
            if (pathParent != null) {
                File parent = pathParent.toFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
            }

            OutputStream out = path.getFileSystem().provider().newOutputStream(path, openOptions);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder))) {
                for (CharSequence line : lines) {
                    writer.append(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
