package com.chhd.volumewaveview.demo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
    }
}
