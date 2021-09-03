package com.zr.ijkaudioplayer.manager;

import static android.os.Environment.MEDIA_MOUNTED;

import android.content.Context;
import android.os.Environment;

import com.zr.ijkaudioplayer.audioCache.CacheListener;
import com.zr.ijkaudioplayer.audioCache.HttpProxyCacheServer;
import com.zr.ijkaudioplayer.audioCache.file.Md5FileNameGenerator;
import com.zr.ijkaudioplayer.utils.LogUtils;

import java.io.File;

/**
 * @author chenpin
 */
public class AudioCacheManager {
    /**
     * 最大缓存容量
     */
    private static final long DEFAULT_MAX_SIZE = 200 * 1024 * 1024;
    /**
     *  最大缓存数量
     */
    private static final int DEFAULT_MAX_FILE_COUNT = 20;
    /**
     * SD卡APP保存文件名
     */
    private static final String SAVE_AUDIO_PATH = "audio_cache";


    private static AudioCacheManager mInstance;
    private static HttpProxyCacheServer mCacheServer;
    private CacheListener mCacheListener;
 
    public static AudioCacheManager getInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (null == mInstance) {
            synchronized (AudioCacheManager.class) {
                if (null == mInstance) {
                    mInstance = new AudioCacheManager(applicationContext);
                }
            }
        }
        return mInstance;
    }
 
    private AudioCacheManager(Context context) {
        File cacheDir = getCacheDirectory(context);
        LogUtils.d("getPath: " + cacheDir.getPath());
        Md5FileNameGenerator md5FileNameGenerator = new Md5FileNameGenerator();
        HttpProxyCacheServer.Builder builder = new HttpProxyCacheServer.Builder(context)
                .maxCacheFilesCount(DEFAULT_MAX_FILE_COUNT).cacheDirectory(cacheDir).fileNameGenerator(md5FileNameGenerator);
        mCacheServer = builder.build();
    }
 
    static HttpProxyCacheServer getProxy(Context context) {
        return mCacheServer == null ? (mCacheServer = newProxy(context)) : mCacheServer;
    }
 
    private static HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer.Builder(context)
                .maxCacheSize(DEFAULT_MAX_SIZE)
                .build();
    }

    private File getCacheDirectory(Context context) {
        File cacheParentDir = getCacheParentDirectory(context);
        File cacheDir = new File(cacheParentDir, SAVE_AUDIO_PATH);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private File getCacheParentDirectory(Context context) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) {
            externalStorageState = "";
        }
        if (MEDIA_MOUNTED.equals(externalStorageState)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = context.getCacheDir().getPath();
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }
 
    private static File getExternalCacheDir(Context context) {
        String pathPrix = context.getCacheDir().getPath();
        File file = new File(pathPrix);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }


    String getProxyUrl(String url) {
        return mCacheServer.getProxyUrl(url);
    }
 
    void registerCacheListener(String url, CacheListener listener) {
        mCacheListener = listener;
        mCacheServer.registerCacheListener(listener, url);
    }
 
    void unregisterCacheListener() {
        mCacheServer.unregisterCacheListener(mCacheListener);
    }
}