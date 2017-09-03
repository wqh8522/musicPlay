package com.wqh.musicplayer.bean;

/**
 * Created by wqh on 2016/12/28.
 */

public class lrcUtil {
    private long currTime;
    private String currLrc;

    public long getCurrTime() {
        return currTime;
    }

    public void setCurrTime(long currTime) {
        this.currTime = currTime;
    }

    public String getCurrLrc() {
        return currLrc;
    }

    public void setCurrLrc(String currLrc) {
        this.currLrc = currLrc;
    }

    @Override
    public String toString() {
        return "lrcUtil{" +
                "currTime=" + currTime +
                ", currLrc='" + currLrc + '\'' +
                '}';
    }
}
