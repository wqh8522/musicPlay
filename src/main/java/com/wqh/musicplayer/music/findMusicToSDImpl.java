package com.wqh.musicplayer.music;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.wqh.musicplayer.bean.musicInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wqh on 2016/12/22.
 */

public class findMusicToSDImpl implements findMusicToSD {
    public List<musicInfo> getMusicData(Context context) {
        List<musicInfo> musicDataList = new ArrayList<musicInfo>();//保存查询到所有音乐
        //媒体库查询，只查找music
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //判断是否得到本地音乐
        if (cursor != null) {
            //遍历cursor
            while (cursor.moveToNext()) {
                musicInfo musicData = new musicInfo();
                musicData.setMusicId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));//音乐Id
                musicData.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));//音乐名字
                musicData.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题
                musicData.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));//艺术家
                musicData.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));//唱片
                musicData.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));//时长
                musicData.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));//大小
                musicData.setIsMuaic(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)));//是否是音乐
                musicData.setUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                musicData.setMusicStatus(true);
//                Toast.makeText(context,musicData.toString(),Toast.LENGTH_LONG).show();
                //判断音乐的时长，只把大于1分钟的音乐添加到集合中
                if (musicData.getName().contains(".mp3") && musicData.getDuration() / (1000 * 6) >= 1) {
                    musicDataList.add(musicData);
                }

            }
        }
        cursor.close();
        return musicDataList;
    }


}
