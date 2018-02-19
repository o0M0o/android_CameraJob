package com.wxm.camerajob.data.define;

import java.util.Calendar;
import java.util.function.Function;

/**
 * time gap for camera job
 * Created by ookoo on 2018/2/19.
 */
public enum TimeGap {
    GAP_FIFTEEN_SECOND("15秒",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 15;
        }),
    GAP_THIRTY_SECOND("30秒",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        }),

    GAP_ONE_MINUTE("1分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec;
        }),
    GAP_FIVE_MINUTE("5分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            int curmin = cr.get(Calendar.MINUTE);
            return (0 == curmin % 5) && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec);
        }),
    GAP_TEN_MINUTE("10分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            int curmin = cr.get(Calendar.MINUTE);
            return (0 == curmin % 10) && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec);
        }),
    GAP_THIRTY_MINUTE("30分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            int curmin = cr.get(Calendar.MINUTE);
            return (0 == curmin % 30) && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec);
        }),

    GAP_ONE_HOUR("1小时",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        }),
    GAP_TWO_HOUR("2小时",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        }),
    GAP_FOUR_HOUR("4小时",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        });

    private String szName;
    private Function<Calendar, Boolean> funArrive;

    TimeGap(String name,  Function<Calendar, Boolean> funer)    {
        szName = name;
        funArrive = funer;
    }

    /**
     * get time gap name
     * @return  name
     */
    public String getGapName()  {
        return szName;
    }

    /**
     * check time gap is arrive
     * @param cr    calendar
     * @return  true if arrive;
     */
    public boolean isArrive(Calendar cr)  {
        return funArrive.apply(cr);
    }
}

