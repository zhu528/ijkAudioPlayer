package com.zr.ijkaudioplayer.bean;

public class SongInfo {
    String songId;      //音乐id
    String songUrl;     //音乐播放地址
    String songName;    //音乐标题
    String artist;      //作者
    String songCover;   //音乐封面
    String duration;    //音乐长度

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSongCover() {
        return songCover;
    }

    public void setSongCover(String songCover) {
        this.songCover = songCover;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "SongInfo{" +
                "songId='" + songId + '\'' +
                ", songUrl='" + songUrl + '\'' +
                ", songName='" + songName + '\'' +
                ", artist='" + artist + '\'' +
                ", songCover='" + songCover + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
