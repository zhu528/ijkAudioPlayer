package com.zr.ijkaudioplayer.bean;

public class SongInfo {
    String song_Id;             //音乐id
    String songPlay_Url;        //音乐播放url和path
    String songPlay_Uri;        //音乐播放uri
    String song_Name;           //音乐标题
    String artist;              //作者
    String song_Cover;          //音乐封面
    String duration;            //音乐长度

    public String getSong_Id() {
        return song_Id;
    }

    public void setSong_Id(String song_Id) {
        this.song_Id = song_Id;
    }

    public String getSongPlay_Url() {
        return songPlay_Url;
    }

    public void setSongPlay_Url(String songPlay_Url) {
        this.songPlay_Url = songPlay_Url;
    }

    public String getSongPlay_Uri() {
        return songPlay_Uri;
    }

    public void setSongPlay_Uri(String songPlay_Uri) {
        this.songPlay_Uri = songPlay_Uri;
    }

    public String getSong_Name() {
        return song_Name;
    }

    public void setSong_Name(String song_Name) {
        this.song_Name = song_Name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSong_Cover() {
        return song_Cover;
    }

    public void setSong_Cover(String song_Cover) {
        this.song_Cover = song_Cover;
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
                "song_Id='" + song_Id + '\'' +
                ", songPlay_Url='" + songPlay_Url + '\'' +
                ", songPlay_Uri='" + songPlay_Uri + '\'' +
                ", song_Name='" + song_Name + '\'' +
                ", artist='" + artist + '\'' +
                ", song_Cover='" + song_Cover + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
