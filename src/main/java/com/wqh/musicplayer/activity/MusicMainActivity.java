package com.wqh.musicplayer.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusicplayer.R;
import com.wqh.musicplayer.bean.musicInfo;
import com.wqh.musicplayer.commons.MusicPalyMode;
import com.wqh.musicplayer.music.findMusicToSD;
import com.wqh.musicplayer.music.findMusicToSDImpl;
import com.wqh.musicplayer.musicutil.TimeUtil;
import com.wqh.musicplayer.service.myMusicService;
import com.wqh.musicplayer.view.LrcView;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class MusicMainActivity extends baseActivity {


    private ImageButton buttonPlayPre;
    private ImageButton buttonPlayNext;
    private ImageButton buttonPlay;
    public static TextView textViewCurTime;
    private TextView textViewTotalTime;
    private ImageButton btnsearch_interent;
    private TextView playMusicName;
    private TextView playMusicArtist;
    private ImageButton buttonPause;
    private ImageButton buttonMode;
    private ImageView myImage;
    private ImageView discImage;
    public static SeekBar seekBar;
    public static LrcView lrcView;

    private int id = 0;
    private ImageButton buttonMenu;
    private List<musicInfo> musicData;//保存所有歌曲
    private musicInfo playingMusic;//正在播放的歌曲
    private findMusicToSD findMusicToSD = new findMusicToSDImpl();
    private Intent serviceintent;
    private Boolean isPause = true;
    private String mode = MusicPalyMode.MUSIC_MODE_CIRCILATE;//音乐播放的模式；
    private static MusicMainActivity.myBroadcastReceiver myBroadcastReceiver;
    private int musicIndex;
    private Bitmap image;
    private long currentValue;
    private ObjectAnimator objectAnimator;
    private Handler handler;
    private long preTime = 0;

    public MusicMainActivity() {
    }

    @Override
    public void init() {
        buttonMenu = (ImageButton) findViewById(R.id.buttonMenu);
        buttonMode = (ImageButton) findViewById(R.id.buttonMode);
        buttonPlayPre = (ImageButton) findViewById(R.id.buttonPlayPre);
        buttonPlayNext = (ImageButton) findViewById(R.id.buttonPlayNext);
        buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        textViewCurTime = (TextView) findViewById(R.id.textViewCurTime);
        textViewTotalTime = (TextView) findViewById(R.id.textViewTotalTime);
        btnsearch_interent = (ImageButton) findViewById(R.id.search_interent);
        playMusicName = (TextView) findViewById(R.id.play_music_name);
        playMusicArtist = (TextView) findViewById(R.id.play_music_artist);
        buttonPause = (ImageButton) findViewById(R.id.buttonPause);
        myImage = (ImageView) findViewById(R.id.myImage);
        discImage = (ImageView) findViewById(R.id.disc_image);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        lrcView = (LrcView) findViewById(R.id.lrc_View);

        musicData = findMusicToSD.getMusicData(MusicMainActivity.this);
        if (musicData.size() != 0) {
            for (musicInfo m : musicData) {
                m.setId(id++);
            } //得到本地所有歌曲
            playingMusic = getRandomMusic();
            setMusicName(playingMusic);
            setImageSrc(playingMusic);
            seekBar.setMax((int) playingMusic.getDuration());
//        初始化Intent，并与service建立连接
            serviceintent = new Intent();
            serviceintent.setClass(MusicMainActivity.this, myMusicService.class);
            serviceintent.putExtra("musicList", (Serializable) musicData);
            serviceintent.putExtra("musicDate", playingMusic);

//            Toast.makeText(MusicMainActivity.this,path,Toast.LENGTH_LONG).show();
            bindService(serviceintent, connection, Service.BIND_AUTO_CREATE);
//            startService(serviceintent);
            registerMusicBroadcast();
            MusicMainActivity.seekBar.setMax((int) playingMusic.getDuration());
            initAnimator();
            setTotalTime();

            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0x123) {
//                        Toast.makeText(MusicMainActivity.this,msg.arg1+"",Toast.LENGTH_LONG).show();
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }

    //初始化ObjectAnimator
    public void initAnimator() {
        objectAnimator = ObjectAnimator.ofFloat(myImage, "rotation", currentValue - 360, currentValue);
        objectAnimator.setDuration(12000);
        objectAnimator.setRepeatCount(-1);//设置动画的重复次数
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);//设置重复模式
        LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
        objectAnimator.setInterpolator(lin);
    }


    @Override
    public void initCreate() {
        setContentView(R.layout.main);
    }

    @Override
    public void setListener() {
        if (musicData.size() != 0) {
            buttonMenu.setOnClickListener(new btnListern());
            buttonPlay.setOnClickListener(new btnListern());
            buttonPause.setOnClickListener(new btnListern());
            buttonPlayNext.setOnClickListener(new btnListern());
            buttonPlayPre.setOnClickListener(new btnListern());
            buttonMode.setOnClickListener(new btnListern());
            seekBar.setOnSeekBarChangeListener(new btnListern());

        }
        btnsearch_interent.setOnClickListener(new btnListern());
    }

    //启动应用，随机获取一首歌曲
    private musicInfo getRandomMusic() {
        int index;
        Random random = new Random();
        index = random.nextInt(musicData.size()) + 0;
        musicInfo randomMusic = musicData.get(index);
        randomMusic.setMusicStatus(isPause);
        return randomMusic;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x123 && resultCode == 0x124) {
            //得到返回的正在播放的歌曲的值
            playingMusic = (musicInfo) data.getSerializableExtra("playingMusic");
            musicData = (List<musicInfo>) data.getSerializableExtra("musicData");
            if (playingMusic == null) {
                return;
            }
            isPause = playingMusic.getMusicStatus();
            rotateImage(isPause);
            setMusicName(playingMusic);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //得到歌曲的专辑图片
    private Bitmap getMusicImage(musicInfo music) {
        Bitmap bitmap = null;
        if (music != null) {
            if (music.getUrl() != null) {
                //能够获取多媒体文件元数据的类
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(music.getUrl()); //设置数据源
                    if (retriever == null) {
                        return null;
                    }
                    byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
                    if (embedPic == null) {
                        return null;
                    }
                    bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        retriever.release();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    //将位图转化为圆形
    public static Bitmap toRoundCornerImage(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        // 抗锯齿
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    //设置专辑图片
    private void setImageSrc(musicInfo music) {
        if (music == null) {
            return;
        }
        Bitmap bitmap = getMusicImage(music);
        if (bitmap != null) {
//            myImage.setImageResource(R.drawable.player_play);
            image = toRoundCornerImage(bitmap, 1000);
            myImage.setImageBitmap(image);
        } else {
            myImage.setImageBitmap(null);
        }
    }

    private void setMusicName(musicInfo playingMusic) {
        playMusicName.setText(playingMusic.getTitle());
        playMusicArtist.setText("--" + playingMusic.getArtist());
        if (playingMusic.getMusicStatus()) {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.GONE);
        } else {
            buttonPlay.setVisibility(View.GONE);
            buttonPause.setVisibility(View.VISIBLE);
        }
    }

    //旋转图片
    private void rotateImage(boolean flag) {
        if (flag) {
            objectAnimator.pause();
            currentValue = objectAnimator.getCurrentPlayTime();
//            Toast.makeText(MusicMainActivity.this, currentValue+"", Toast.LENGTH_LONG).show();
        } else {
            if (objectAnimator.isPaused()) {
                objectAnimator.resume();
            } else {
                objectAnimator.start();
            }
//            objectAnimator.setCurrentPlayTime((long) currentValue);
        }
    }


    private ServiceConnection connection = new ServiceConnection() {
        //当activity与service绑定成功时回调改方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        //当activity与service断开连接时回调改方法
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //设置歌曲的总的时间
    private void setTotalTime() {
        String toaltTime = TimeUtil.formatTime((int) playingMusic.getDuration());
        textViewTotalTime.setText(toaltTime);
    }

    //内部类设置控件的事件
    class btnListern implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonMenu:
                    musicListListener();
                    break;
                case R.id.buttonPlay:
                    btnPlayListener(false);
                    break;
                case R.id.buttonPause:
                    btnPlayListener(true);
                    break;
                case R.id.buttonPlayNext:
                    btnPlayNextAndPre("next");
                    break;
                case R.id.buttonPlayPre:
                    btnPlayNextAndPre("pre");
                    break;
                case R.id.buttonMode:
                    btnPlayMode();
                    break;
            }
        }

        //使用广播向后台service发送播放模式
        private void btnPlayMode() {
            if (mode == MusicPalyMode.MUSIC_MODE_CIRCILATE) {
                mode = MusicPalyMode.MUSIC_MODE_RANDOM;
                buttonMode.setImageResource(R.drawable.mode_random);
                Toast.makeText(MusicMainActivity.this, "随机播放", Toast.LENGTH_LONG).show();
            } else if (mode == MusicPalyMode.MUSIC_MODE_RANDOM) {
                mode = MusicPalyMode.MUSIC_MODE_ONE;
                buttonMode.setImageResource(R.drawable.mode_one);
                Toast.makeText(MusicMainActivity.this, "单曲循环", Toast.LENGTH_LONG).show();
            } else if (mode == MusicPalyMode.MUSIC_MODE_ONE) {
                mode = MusicPalyMode.MUSIC_MODE_CIRCILATE;
                buttonMode.setImageResource(R.drawable.mode_circulate);
                Toast.makeText(MusicMainActivity.this, "列表循环", Toast.LENGTH_LONG).show();
            }
            sendMusicBroadcast(mode);
        }

        //发送播放模式的广播
        public void sendMusicBroadcast(String mode) {
            Intent intent = new Intent();
            intent.putExtra("mode", mode);
            intent.setAction("com.wqh.musicplayer.activity.MusicMainActivity");
            sendBroadcast(intent);
        }

        //跳转到音乐列表页
        private void musicListListener() {
            // unregisterReceiver(myBroadcastReceiver);
            Intent intent = new Intent();
            intent.setClass(MusicMainActivity.this, MusicListActivity.class);
            playingMusic.setMusicStatus(isPause);
            intent.putExtra("musicData", (Serializable) musicData);
            intent.putExtra("playingMusic", playingMusic);
            startActivityForResult(intent, 0x123);
//            unregisterReceiver(myBroadcastReceiver);
        }

        private void btnPlayListener(boolean flag) {
            isPause = flag;
//            Intent intent = new Intent();
//            intent.setClass(MusicMainActivity.this, myMusicService.class);
            playingMusic.setMusicStatus(flag);
            serviceintent.putExtra("musicData", playingMusic);
            startService(serviceintent);
            setMusicName(playingMusic);
            rotateImage(flag);
            setTotalTime();
        }

        private void btnPlayNextAndPre(String string) {
            if (string.equals("next")) {
                if (mode.equals(MusicPalyMode.MUSIC_MODE_CIRCILATE) || mode.equals(MusicPalyMode.MUSIC_MODE_ONE)) {
                    int index = (int) (playingMusic.getId() + 1);
                    if (index < musicData.size()) {
                        playingMusic = musicData.get(index);
                    } else {
                        playingMusic = musicData.get(0);
                    }
                } else if (mode.equals(MusicPalyMode.MUSIC_MODE_RANDOM)) {
                    playingMusic = getRandomMusic();
                }
            } else if (string.equals("pre")) {
                if (mode.equals(MusicPalyMode.MUSIC_MODE_CIRCILATE) || mode.equals(MusicPalyMode.MUSIC_MODE_ONE)) {
                    int index = (int) (playingMusic.getId() - 1);
                    if (index >= 0) {
                        playingMusic = musicData.get(index);
                    } else {
                        playingMusic = musicData.get(musicData.size() - 1);
                    }
                } else if (mode.equals(MusicPalyMode.MUSIC_MODE_RANDOM)) {
                    playingMusic = getRandomMusic();
                }
            }
            seekBar.setProgress(0);
//            myMusicService.lrcIndex(playingMusic);
            setImageSrc(playingMusic);
            btnPlayListener(false);
        }

        int progres;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            progres = progress;
            textViewCurTime.setText(TimeUtil.formatTime(progres));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            myMusicService.setMusicTime(progres);
        }
    }

    //接收广播
    class myBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            musicIndex = intent.getIntExtra("position", -1);//当前播放的歌曲的索引
            playingMusic = musicData.get(musicIndex);
            playingMusic.setMusicStatus(false);
            playMusicName.setText(playingMusic.getTitle());
            playMusicArtist.setText("--" + playingMusic.getArtist());
            setImageSrc(playingMusic);
            setTotalTime();
        }

    }

    //接收广播
    private void registerMusicBroadcast() {
        //注册广播接收器
        myBroadcastReceiver = new myBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.wqh.musicplayer.service.myMusicService");
        MusicMainActivity.this.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);
        stopService(serviceintent);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (System.currentTimeMillis() - preTime > 2000) {
                    preTime = System.currentTimeMillis();
                    Toast.makeText(MusicMainActivity.this, "再按一次返回键退出！", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
                break;
        }
        return false;
    }
}
