package com.zr.ijkaudioplayer.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String rootPath = Environment.getExternalStorageDirectory().getPath();
    public static String playerPath = rootPath + File.separator + "music";

    /**
     * 将毫秒转换年月日时分秒
     */
    public static String getCachePath(String url){
        return new File(playerPath, Utils.getNameByPath(url) + System.currentTimeMillis() + "." + Utils.getExtensionWithFilePath(url)).getPath();
    }

    /**
     * 将毫秒转换年月日时分秒
     */
    public static String getTimeByMillSecond(long time, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }

    /**
     * 根据文件路径获取后缀
     */
    public static String getExtensionWithFilePath(String filePath) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (TextUtils.isEmpty(extension)) {
            // 有的内部路径，上面的api获取不到后缀，需要重新获取一次
            int index = filePath.lastIndexOf(".");
            if (index == -1 || index == filePath.length() - 1) {
                return null;
            } else {
                return filePath.substring(index + 1).toLowerCase();
            }
        }
        return extension;
    }

    /**
     * 根据文件路径获取名字
     *
     * @return 文件名
     */
    public static String getNameByPath(String path) {
        int start = path.lastIndexOf("/");

        if (start != -1) {
            return path.substring(start + 1);// 包含头不包含尾 , 故:头 + 1
        } else {
            return path;
        }
    }

    public static String formatMusicTime(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round(seconds / (double) 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }
}
