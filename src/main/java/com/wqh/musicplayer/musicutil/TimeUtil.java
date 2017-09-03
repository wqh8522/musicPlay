package com.wqh.musicplayer.musicutil;

/**
 * Created by wqh on 2016/12/25.
 */

public class TimeUtil {
    //格式化获取到的时间
    public static String formatTime(int time) {
        if (time / 1000 % 60 < 10) {
            return time / 1000 / 60 + ":0" + time / 1000 % 60;
        } else {
            return time / 1000 / 60 + ":" + time / 1000 % 60;
        }

    }
}
