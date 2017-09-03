package com.wqh.musicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.mymusicplayer.R;
import com.wqh.musicplayer.activity.MusicMainActivity;
import com.wqh.musicplayer.bean.Lrc;
import com.wqh.musicplayer.bean.musicInfo;
import com.wqh.musicplayer.commons.MusicPalyMode;
import com.wqh.musicplayer.music.LrcProcess;
import com.wqh.musicplayer.musicutil.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wqh on 2016/12/23.
 */

public class myMusicService extends Service {
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    private List<musicInfo> musicData = new ArrayList<musicInfo>();//得到所有音乐信息
    private String musicPath;//播放的地址
    private static musicInfo playingMusic;//要播放的歌曲
    private int playingMusicIndex = -1;//正在播放的歌曲的索引
    private boolean isPause = false;//记录是否是暂停状态
    private String playMusicMode = MusicPalyMode.MUSIC_MODE_CIRCILATE;//播放的模式,默认为列表循环
    private myBroadcastReceiver myBroadcastReceiver;
    private String currTime;//记录当前歌曲播放的进度
    private Handler handler;
    private static LrcProcess lrcProcess;
    private Binder binder;
    private myThread thread = new myThread(isPause);
    private static Lrc lrc;

    private int currentTime;
    private int duration;
    private int index;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setListener() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(myMusicService.this,playMusicMode,Toast.LENGTH_LONG).show();
                int index = 0;
                //当前歌曲播放完毕后播放下一首歌曲，
                // 首先判断播放的模式
                if (playMusicMode.equals(MusicPalyMode.MUSIC_MODE_CIRCILATE)) {
                    index = (int) (playingMusic.getId() + 1);
                    if (index > musicData.size() - 1) {
                        index = 0;
                    }
                    playingMusic = musicData.get(index);
                } else if (playMusicMode.equals(MusicPalyMode.MUSIC_MODE_ONE)) {

                } else if (playMusicMode.equals(MusicPalyMode.MUSIC_MODE_RANDOM)) {
                    Random random = new Random();
                    index = random.nextInt(musicData.size()) + 0;
                    playingMusic = musicData.get(index);
                }
                playMusic(mediaPlayer, playingMusic);
                MusicMainActivity.seekBar.setMax((int) playingMusic.getDuration());
                sendMusicBroadcast(playingMusicIndex, isPause);
            }
        });
    }

    @Override
    public void onCreate() {
        //连接成功后注册广播接收器
        receiveMusicBroadcast();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x123:
                        currTime = (String) msg.obj;
                        MusicMainActivity.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        if (mediaPlayer.getCurrentPosition() > 100) {
                            MusicMainActivity.textViewCurTime.setText(currTime);
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
        thread.start();
        super.onCreate();
    }

    public void initLrc(musicInfo music) {
        lrc = new Lrc();
        lrcProcess = new LrcProcess();
        //读取歌词文件
        try {
            //传回处理后的歌词文件
            Lrc lrc1 = lrcProcess.parser(music);
//            Toast.makeText(myMusicService.this,lrc1.toString(),Toast.LENGTH_LONG).show();
            Log.d("wqh", lrc1.toString());
            if (lrc1.getInfos()!= null) {
                lrc = lrc1;
                MusicMainActivity.lrcView.setLrc(lrc);

                //切换带动画显示歌词
                MusicMainActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(myMusicService.this, R.anim.lrc_alpha));
                handler.post(mRunnable);
            } else {
                Toast.makeText(myMusicService.this,lrc.toString(),Toast.LENGTH_LONG).show();
                MusicMainActivity.lrcView.setGravity(Gravity.CENTER);
                MusicMainActivity.lrcView.setText("没有歌词文件，请下载....");
                MusicMainActivity.lrcView.invalidate();
            }
        } catch (Exception e) {
            lrc = null;
            MusicMainActivity.lrcView.setGravity(Gravity.CENTER);
            MusicMainActivity.lrcView.setText("没有歌词文件，请下载....");
            MusicMainActivity.lrcView.invalidate();

        }

    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                MusicMainActivity.lrcView.setIndex(lrcIndex());
                MusicMainActivity.lrcView.invalidate();
                handler.postDelayed(mRunnable, 100);
            } catch (Exception e) {
                MusicMainActivity.lrcView.setText("没有歌词文件，请下载....");
            }

        }
    };

    // 根据时间获取歌词显示的索引值
    public int lrcIndex() {
        //判断是否处于播放状态
        if (mediaPlayer.isPlaying()) {
            currentTime = mediaPlayer.getCurrentPosition();
            duration = mediaPlayer.getDuration();
        }
        if (currentTime < duration) {
            for (int i = 0; i < lrc.getInfos().size(); i++) {
                if (i < lrc.getInfos().size() - 1) {
                    if (currentTime < lrc.getInfos().get(i).getCurrTime() && i == 0) {
                        index = i;
                    }
                    if (currentTime > lrc.getInfos().get(i).getCurrTime() && currentTime < lrc.getInfos().get(i + 1).getCurrTime()) {
                        index = i;
                    }
                }
                if (i == lrc.getInfos().size() - 1 && currentTime > lrc.getInfos().get(i).getCurrTime()) {
                    index = i;
                }
            }
        }
        return index;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);
        mediaPlayer.stop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //建立连接是调用改方法
        musicData = (List<musicInfo>) intent.getSerializableExtra("musicList");
        //    Toast.makeText(myMusicService.this,musicData.size()+"",Toast.LENGTH_LONG).show();
        playingMusic = (musicInfo) intent.getSerializableExtra("musicData");
        try {
            if (musicData.size() != 0 && playingMusic != null) {
                playMusicStatus(playingMusic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setListener();
        return super.onStartCommand(intent, flags, startId);
    }

    //更改音乐的状态
    private void playMusicStatus(musicInfo music) throws IOException {
        if (music != null) {
            if (playingMusicIndex != music.getId()) {
                playMusic(mediaPlayer, music);
            } else {
                if (!isPause) {
                    mediaPlayer.pause();
                    isPause = true;
                } else {
                    mediaPlayer.start();
                    isPause = false;
                }
            }
            sendMusicBroadcast(playingMusicIndex, isPause);
        }

    }
    //播放音乐
    private void playMusic(MediaPlayer mediaPlayer, musicInfo music) {
        try {
            mediaPlayer.reset();
            musicPath = music.getUrl();
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPause = false;
            playingMusicIndex = (int) music.getId();//记录当前播放的歌曲的Id
            initLrc(playingMusic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据进度条的值改变音乐的当前播放时间
    public static void setMusicTime(int progress) {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.seekTo(progress);
    }

    //发送播放状态的广播
    public void sendMusicBroadcast(int playingMusicIndex, boolean isPause) {
        Intent intent = new Intent();
        intent.putExtra("position", playingMusicIndex);
        intent.putExtra("isPause", isPause);
        intent.setAction("com.wqh.musicplayer.service.myMusicService");
        sendBroadcast(intent);
    }

    //接收关于播放模式的广播
    class myBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            playMusicMode = intent.getStringExtra("mode");
        }
    }

    //接收广播
    private void receiveMusicBroadcast() {
        //注册广播接收器
        myBroadcastReceiver = new myBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.wqh.musicplayer.activity.MusicMainActivity");
        myMusicService.this.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    class myThread extends Thread {

        boolean flag;

        myThread(boolean flag) {
            this.flag = flag;
        }

        @Override
        public void run() {
            //  Toast.makeText(myMusicService.this,count++,Toast.LENGTH_LONG).show();
            while (!flag) {
                try {
//                    MusicMainActivity.textViewCurTime.setText(TimeUtil.formatTime(mediaPlayer.getCurrentPosition()));
                    handler.sendEmptyMessage(1);
                    Message msg = new Message();
                    msg.what = 0x123;
                    msg.obj = TimeUtil.formatTime(mediaPlayer.getCurrentPosition());
                    handler.sendMessage(msg);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
