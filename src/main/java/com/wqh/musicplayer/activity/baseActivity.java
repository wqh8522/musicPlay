package com.wqh.musicplayer.activity;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by wqh on 2016/12/22.
 */

public abstract class baseActivity extends Activity {

    public abstract void init();

    public abstract void initCreate();

    public abstract void setListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCreate();
        init();
        setListener();
    }
}
