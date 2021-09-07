package com.zr.ijkaudioplayer;

import android.net.Uri;

import com.zr.ijkaudioplayer.bean.SongInfo;
import com.zr.ijkaudioplayer.listener.AudioPlayerListener;

import java.util.List;

public abstract class MyAbstractAudioPlayer {
 
    AudioPlayerListener mAudioPlayerEvent;
    /**
     * 暂停之后调用，接着上一次播放
     */
    public abstract void reStart();
    /**
     * 带资源播放，播放当前资源
     * @param path path
     */
    public abstract void start(String path);

    /**
     * uri资源播放，播放当前资源
     * @param uri uri
     */
    public abstract void startUri(Uri uri);

    /**
     * 将整个列表添加到播放列表，如果是未播放状态，则播放第一个
     * @param pathList 资源列表
     */
    public abstract void startList(List<SongInfo> pathList, int playPosition, boolean isCache);
 
    /**
     * 播放事件监听器
     * @param event 时间
     */
    public abstract void setAudioPlayerListener(AudioPlayerListener event);
 
    /**
     * 暂停
     */
    public abstract void pause();

    public abstract void stop();

    /**
     * 暂停缓存
     */
    public abstract void stopCacheAndShutdown();

    /**
     * 下一首
     */
    public abstract void nextPlay();
 
    /**
     * 上一首
     */
    public abstract void prevPlay();
 
    /**
     * 是否正在播放
     * @return boolean
     */
    public abstract boolean isPlaying();
 
    /**
     * 当前播放状态
     * @return AudioPlayEnum
     */
    public abstract AudioPlayEnum getPlayerStatus();
 
    /**
     * 调整进度
     * @param time 时间
     */
    public abstract void seekTo(long time);
 
    /**
     * 拖动进度条，通知（防止拖动时Timmer跑进度条）
     */
    public abstract void seekStart();
 
    /**
     * 释放播放器
     */
    public abstract void release();
 
    /**
     * 获取当前播放的位置
     * @return 获取当前播放的位置
     */
    public abstract long getCurrentPosition();
 
    /**
     * 获取音频总时长
     * @return long
     */
    public abstract long getDuration();
 
    /**
     * 获取缓冲百分比
     * @return int
     */
    public abstract int getBufferedPercentage();
    /**
     * 设置列表是否循环播放
     * @param isLooping 循环
     */
    public abstract void setListLooping(boolean isLooping);
    /**
     * 设置是否单曲循环
     * @param isLooping 循环
     */
    public abstract void setSingleLooping(boolean isLooping);
}