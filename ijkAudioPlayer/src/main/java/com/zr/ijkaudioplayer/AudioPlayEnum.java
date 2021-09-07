package com.zr.ijkaudioplayer;

public enum AudioPlayEnum {
    /**播放空闲*/
    PLAYER_FREE,
    /**预缓冲，准备播放中*/
    PLAYER_PREPARING,
    /**播放中*/
    PLAYER_PLAYING,
    /**播放完毕*/
    PLAYER_COMPLETE,
    /**播放暂停*/
    PLAYER_PAUSE,
    /**播放错误*/
    PLAYER_ERROR,
    /**播放路径为空*/
    PLAYER_NONE
}