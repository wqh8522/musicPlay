package com.wqh.musicplayer.bean;

import java.io.Serializable;

/**
 * Created by wqh on 2016/12/22.
 * mp3的实体类
 */

public class musicInfo implements Serializable{
    private long id;
    private long musicId;
    private String name;//歌曲名
    private String title;//歌曲名
    private String artist;//艺术家
    private long duration;//持续时间
    private long size;//大小
    private String url;//路径
    private String album;//所属唱片
    private int isMuaic;
    private String lrcUrl;
    private Boolean musicStatus;



    public Boolean getMusicStatus() {
        return musicStatus;
    }

    public void setMusicStatus(Boolean musicStatus) {
        this.musicStatus = musicStatus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getIsMuaic() {
        return isMuaic;
    }

    public void setIsMuaic(int isMuaic) {
        this.isMuaic = isMuaic;
    }

    public long getMusicId() {
        return musicId;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public String getLrcUrl() {
        return lrcUrl;
    }

    public void setLrcUrl(String lrcUrl) {
        this.lrcUrl = lrcUrl;
    }

    @Override
    public String toString() {
        return "musicInfo{" +
                "id=" + id +
                ", musicId=" + musicId +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", album='" + album + '\'' +
                ", isMuaic=" + isMuaic +
                ", lrcUrl='" + lrcUrl + '\'' +
                ", musicStatus=" + musicStatus +
                '}';
    }
}
