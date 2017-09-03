package com.wqh.musicplayer.bean;

import java.util.HashMap;
import java.util.List;

/**
 * Created by wqh on 2016/12/26.
 */
//歌词的试实体类
public class Lrc {
    private String title;//歌曲名
    private String singer;//演唱者
    private String album;//专辑
    private List<lrcUtil> infos;

    public List<lrcUtil> getInfos() {
        return infos;
    }

    public void setInfos(List<lrcUtil> infos) {
        this.infos = infos;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "Lrc{" +
                "title='" + title + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", infos=" + infos +
                '}';
    }
}
