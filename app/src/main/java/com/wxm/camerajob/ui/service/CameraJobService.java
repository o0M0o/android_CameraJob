package com.wxm.camerajob.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * app的后台任务
 * Created by 123 on 2016/6/9.
 */
public class CameraJobService extends Service {
    private boolean started;
    private boolean threadDisable;
    private static final String TAG = "CameraJobService";


    @Override
    public void onDestroy() {
        super.onDestroy();
        threadDisable = true;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }
}
