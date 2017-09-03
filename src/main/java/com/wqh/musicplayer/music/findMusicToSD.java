package com.wqh.musicplayer.music;

import android.content.Context;

import com.wqh.musicplayer.bean.musicInfo;

import java.util.List;

/**
 * Created by wqh on 2016/12/22.
 */

//从内存种读取音乐
public interface findMusicToSD {
     List<musicInfo> getMusicData(Context context);
}
