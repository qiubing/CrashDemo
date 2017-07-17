package com.example.crashdemo;

import android.app.Application;

/**
 * Description:
 * Author: qiubing
 * Date: 2017-07-17 16:11
 */
public class CrashMonitorApplication extends Application {

    @Override
    public void onCreate() {
        initModule();
    }

    private void initModule(){
        CrashHandler crashHandler = new CrashHandler();
        crashHandler.init(this);
    }

}
