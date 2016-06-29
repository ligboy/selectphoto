package org.ligboy.selectphoto;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Ligboy.Liu ligboy@gmail.com.
 */
final class ContextUtil {

    /**
     * 创建Android data cache 临时文件
     *
     * @param context      Context
     * @param prefix       临时文件前缀.前缀长度不短于3个字符.
     * @param suffix       临时文件后缀.
     * @param subDirectory 子目录名，null则位于cache根目录
     * @return 临时文件
     */
    @Nullable
    public static File createTempFile(@NonNull final Context context, @NonNull String prefix,
                                      @Nullable String suffix, @Nullable String subDirectory) {
        File tempFile = null;
        //当输出文件不存在时，创建
        File cacheDerectory = null;
        File outputDir = null;
        File externalCacheDir = context.getExternalCacheDir();
        //判断是否具有WRITE_EXTERNAL_STORAGE 权限
        if (externalCacheDir != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                || context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
                && externalCacheDir.canWrite()) {
            cacheDerectory = externalCacheDir;
        } else {
            cacheDerectory = context.getCacheDir();
        }
        if (!TextUtils.isEmpty(subDirectory)) {
            outputDir = new File(cacheDerectory, subDirectory);
        } else {
            outputDir = cacheDerectory;
        }
        if (outputDir != null
                && (outputDir.exists()
                || outputDir.mkdirs())) {
            try {
                tempFile = File.createTempFile(prefix, suffix, outputDir);
            } catch (IOException ignored) {
            }
        }

        return tempFile;
    }

    /**
     * 创建Android data cache 临时文件
     *
     * @param context Context
     * @param prefix  临时文件前缀.前缀长度不短于3个字符.
     * @param suffix  临时文件后缀.
     * @return 临时文件
     */
    @Nullable
    public static File createTempFile(@NonNull final Context context, @NonNull String prefix,
                                      @Nullable String suffix) {
        return createTempFile(context, prefix, suffix, null);
    }
}
