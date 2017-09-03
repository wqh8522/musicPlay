package com.wqh.musicplayer.activity;


import android.content.Intent;

import com.example.mymusicplayer.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wqh on 2016/12/29.
 */

public class LoadingActivity extends baseActivity {

    @Override
    public void init() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(LoadingActivity.this, MusicMainActivity.class));
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 2000);
    }

    @Override
    public void initCreate() {
        setContentView(R.layout.loading_main);
        init();
    }

    @Override
    public void setListener() {

    }
}
