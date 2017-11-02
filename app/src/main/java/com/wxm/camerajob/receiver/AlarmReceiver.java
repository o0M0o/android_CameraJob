package com.wxm.camerajob.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.data.define.GlobalDef;
import com.wxm.camerajob.utility.GlobalContext;
import com.wxm.camerajob.utility.ContextUtil;

/**
 * receiver alarm from system
 * Created by 123 on 2016/6/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private final static String TAG = "AlarmReceiver";

    public AlarmReceiver()  {
        super();
    }

    /**
     * wakeup when job active
     * @param arg0   param
     * @param data   param
     */
    @Override
    public void onReceive(Context arg0, Intent data) {
        try {
            Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                    GlobalDef.MSG_TYPE_WAKEUP);
            m.sendToTarget();

            // 设置闹钟
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
