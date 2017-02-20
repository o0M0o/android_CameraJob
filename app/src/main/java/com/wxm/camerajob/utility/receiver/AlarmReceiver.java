package com.wxm.camerajob.utility.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.utility.GlobalContext;
import com.wxm.camerajob.utility.utility.ContextUtil;

/**
 * 该类接收系统定时唤醒闹铃
 * Created by 123 on 2016/6/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private final static String TAG = "AlarmReceiver";

    public AlarmReceiver()  {
        super();
    }

    /**
     * 每十秒唤醒一次APP
     * @param arg0   param
     * @param data   param
     */
    @Override
    public void onReceive(Context arg0, Intent data) {
        //Log.i(TAG, "app wakeup");
        //FileLogger.getLogger().info("camerajob wakeup");

        try {
            Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                    GlobalDef.MSG_TYPE_WAKEUP);
            m.sendToTarget();

            // 设置闹钟
            //FileLogger.getLogger().info("camerajob set alarm again");
            Context ct = ContextUtil.getInstance();
            Intent intent = new Intent(ct, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ct, 0, intent, 0);
            AlarmManager alarmManager =
                    (AlarmManager)ContextUtil.getInstance().getSystemService(Context.ALARM_SERVICE);

            long curms = System.currentTimeMillis();
            long nextms = (curms - (curms % GlobalDef.INT_GLOBALJOB_PERIOD))
                            + GlobalDef.INT_GLOBALJOB_PERIOD;
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextms, pendingIntent);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }
}
