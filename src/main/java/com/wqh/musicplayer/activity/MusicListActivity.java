package com.wqh.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.R;
import com.wqh.musicplayer.adapter.MusicListAdapter;
import com.wqh.musicplayer.bean.musicInfo;
import com.wqh.musicplayer.music.findMusicToSD;
import com.wqh.musicplayer.music.findMusicToSDImpl;
import com.wqh.musicplayer.service.myMusicService;
import com.wqh.musicplayer.web.WebMusic;
import com.wqh.musicplayer.web.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wqh on 2016/12/22.
 */

public class MusicListActivity extends baseActivity implements web{
    private ImageButton btnBack;
    private EditText textSerchMusic;
    private ImageButton btnSerchMusic;
    private Button btnLocalMusic;



    private ListView listView;
    private List<musicInfo> musicData = new ArrayList<musicInfo>();
    private MusicListAdapter musicListAdapter;
    private findMusicToSD findMusicToSD = new findMusicToSDImpl();
    private List<musicInfo> musicList = new ArrayList<musicInfo>();
    private musicInfo music;//正在播放或者列表被选中的歌曲；
    private Boolean isPause = null;//播放状态
    private int musicIndex;//正在播放的音乐的Id
    private Intent intent;
    private Handler handler;
    private myBroadcastReceiver myBroadcastReceiver;

    @Override
    public void init() {
        int id = 0;
        //得到歌曲列表的listView，初始化控件
        listView = (ListView) findViewById(R.id.musiclistview);
        btnBack = (ImageButton) findViewById(R.id.btn_back);
        textSerchMusic = (EditText) findViewById(R.id.text_serchMusic);
        btnSerchMusic = (ImageButton) findViewById(R.id.btn_serchMusic);
        btnLocalMusic = (Button) findViewById(R.id.btn_local_music);
        //扫描本地歌曲
        //将歌曲信息绑定到listView
        getMusicIntent();
        receiveMusicBroadcast();
    }

    @Override
    public void initCreate() {
        setContentView(R.layout.music_list);
    }


    @Override
    public void setListener() {
        listView.setOnItemClickListener(new clickListener());
        btnBack.setOnClickListener(new clickListener());
        btnSerchMusic.setOnClickListener(new clickListener());
        textSerchMusic.setOnEditorActionListener(new clickListener());
        btnLocalMusic.setOnClickListener(new clickListener());
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
    }


    //接收广播
    private void receiveMusicBroadcast() {
        //注册广播接收器
        myBroadcastReceiver = new myBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.wqh.musicplayer.service.myMusicService");
        MusicListActivity.this.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    //得到Intent，并得到正在播放的歌曲
    private void getMusicIntent() {
        intent = getIntent();
        music = (musicInfo) intent.getSerializableExtra("playingMusic");//得到正在播放的歌曲
        musicData = (List<musicInfo>) intent.getSerializableExtra("musicData");
        if (musicData != null) {
            //得到所有歌曲，并装载到listview中
            musicListAdapter = new MusicListAdapter(this, musicData);
            listView.setAdapter(musicListAdapter);
        }
        if (music != null) {
            //改变正在播放的歌曲名的颜色
//            Toast.makeText(MusicListActivity.this,String.valueOf(music.getMusicStatus()),Toast.LENGTH_LONG).show();
            isPause = music.getMusicStatus();
            musicListAdapter.changeNameColor((int) music.getId(), music.getMusicStatus());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                BackLiatener();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void searchMusic() {

        String name = textSerchMusic.getText().toString();
        if (name.equals("")) {
            return;
        } else {
            searchMusicFromWeb(name);
        }
//        int id = 0;
//        for (musicInfo music : musicData) {
////            Toast.makeText(MusicListActivity.this,music.getName(),Toast.LENGTH_LONG).show();
//            if (music.getName().contains(name)) {
//                id = (int) music.getId();
//                listView.setSelection(id);
//                break;
//            }
//        }
//    listView.setSelection(id);
    }

    private void searchMusicFromWeb(String name) {
        WebMusic webMusic=new WebMusic(name);
        webMusic.setListner(MusicListActivity.this);
     //   Toast.makeText(MusicListActivity.this, w.string,Toast.LENGTH_LONG).show();
        if (musicList!= null) {
            musicListAdapter = new MusicListAdapter(this, musicList);
            listView.setAdapter(musicListAdapter);
        }
    }

    @Override
    public void CompleteListener(List<musicInfo> list) {
        int id=0;
//        Log.d("test","--------->"+ list.size() + "");
//        Toast.makeText(MusicListActivity.this,list.get(0).toString()+"",Toast.LENGTH_LONG).show();
        if (list!= null) {
            for (musicInfo m : list) {
                m.setId(id++);
            }
            musicData = list;
            listView.invalidate();
            musicListAdapter = new MusicListAdapter(this, list);
            listView.setAdapter(musicListAdapter);
        }
    }

    class clickListener implements AdapterView.OnItemClickListener, View.OnClickListener, TextView.OnEditorActionListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //得到所要播放的音乐信息
            music = musicData.get(position);
            intent = new Intent();
            intent.setClass(MusicListActivity.this, myMusicService.class);
            intent.putExtra("musicList", (Serializable) musicData);
            music.setMusicStatus(isPause);
            intent.putExtra("musicData", music);
            startService(intent);
            musicListAdapter.changeNameColor(position, isPause);
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_back:
                    BackLiatener();
                    break;
                case R.id.btn_serchMusic:
                    searchMusic();
                    break;
                case R.id.btn_local_music:
                    showAllLocalMusic();
                    break;
            }
        }
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
//                searchMusic();
                return true;
            }
            return false;
        }
    }

    //显示所有本地
    private void showAllLocalMusic(){
        int id=0;
        musicData = findMusicToSD.getMusicData(MusicListActivity.this);
        for (musicInfo m : musicData) {
            m.setId(id++);
        }
        musicListAdapter = new MusicListAdapter(this, musicData);
        listView.setAdapter(musicListAdapter);
    }
    private void BackLiatener() {
        Intent intent = getIntent();
        if (music != null) {
            music.setMusicStatus(isPause);
            intent.putExtra("playingMusic", music);
            intent.putExtra("musicDate", (Serializable) musicData);
        }
        setResult(0x124, intent);
        finish();
        stopService(intent);
    }

    //接收广播
    class myBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            musicIndex = intent.getIntExtra("position", -1);//当前播放的歌曲的索引
            isPause = intent.getBooleanExtra("isPause", true);
            music = musicData.get(musicIndex);
            musicListAdapter.changeNameColor(musicIndex, isPause);
        }
    }
}
