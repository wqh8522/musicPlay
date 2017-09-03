package com.wqh.musicplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.R;
import com.wqh.musicplayer.activity.MusicListActivity;
import com.wqh.musicplayer.bean.musicInfo;
import com.wqh.musicplayer.musicutil.TimeUtil;

import java.util.List;

/**
 * Created by wqh on 2016/12/22.
 */
//自定义adapter，用户绑定歌曲信息到listView
public class MusicListAdapter extends BaseAdapter {
    private List<musicInfo> musicData;
    private Context context;
    private int playMusicIndex;//当前播放音乐的索引
    private boolean isPause = false;//记录当前的播放状态，是否是暂停

    public MusicListAdapter(Context context, List<musicInfo> musicData) {
        this.context = context;
        this.musicData = musicData;
        playMusicIndex = -1;
    }

    public void changeNameColor(int position, boolean isPause) {
        this.playMusicIndex = position;
        this.isPause = isPause;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return musicData.size();
    }

    @Override
    public Object getItem(int position) {
        return musicData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = convertView.inflate(context, R.layout.music_listview, null);
        }

        TextView index = (TextView) convertView.findViewById(R.id.musiclistPos);
        index.setText(String.valueOf(position + 1) + ".");
        TextView musicAritst = (TextView) convertView.findViewById(R.id.musicAritst);
        musicAritst.setText(musicData.get(position).getArtist());
        TextView musicTime = (TextView) convertView.findViewById(R.id.musicTime);
        String time = TimeUtil.formatTime((int) musicData.get(position).getDuration());
        musicTime.setText(time);
        //处理正在播放的歌曲名的颜色
        setMusicNameColor(position, convertView);
        return convertView;
    }
    //修改正在播放的歌曲名的颜色
    public void setMusicNameColor(int position, View view) {
        TextView musicName = (TextView) view.findViewById(R.id.musicName);
        if (position != playMusicIndex) {
            musicName.setTextColor(Color.BLACK);
        } else {
          //  musicName.setTextColor(Color.CYAN);
           if (!isPause) {
                musicName.setTextColor(Color.CYAN);
            } else {
                musicName.setTextColor(Color.RED);
            }
        }
        musicName.setText(musicData.get(position).getTitle());
    }


}
