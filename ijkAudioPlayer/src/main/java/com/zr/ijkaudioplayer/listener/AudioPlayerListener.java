package com.zr.ijkaudioplayer.listener;

import com.zr.ijkaudioplayer.AudioPlayEnum;

public interface AudioPlayerListener {
    /**
     * 缓存更新
     */
    void onBufferingUpdate(int percent);
    /**
     * 是否正在缓冲
     */
    void onBuffering(boolean isBuffering);
    /**
     * 播放进度
     */
    void onPlayProgress(long duration,long currPosition);
    /**
     * 播放状态改变
     * @param mStatus 状态
     */
    void onStatusChange(AudioPlayEnum mStatus, int currPlayPotion);
}