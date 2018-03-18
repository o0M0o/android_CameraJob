package com.wxm.camerajob.data.define;

import java.util.Calendar;
import java.util.function.Function;

import javax.microedition.khronos.opengles.GL;

/**
 * time gap for camera job
 * Created by ookoo on 2018/2/19.
 */
public enum ETimeGap {
    GAP_FIFTEEN_SECOND("15秒",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 15;
        },
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return cursec == 15 ? 15 * GlobalDef.MS_SECOND
                   : (15 - ((long)cursec % 15)) * GlobalDef.MS_SECOND;
        }),
    GAP_THIRTY_SECOND("30秒",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        },
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return 30 == cursec ? 30 * GlobalDef.MS_SECOND
                    : (30 - ((long)cursec % 30)) * GlobalDef.MS_SECOND;
        }),

    GAP_ONE_MINUTE("1分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec;
        },
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return (60 - ((long)cursec % 60)) * GlobalDef.MS_SECOND;
        }),
    GAP_FIVE_MINUTE("5分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            int curmin = cr.get(Calendar.MINUTE);
            return (0 == curmin % 5) && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec);
        },
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return (60 - ((long)cursec % 60)) * GlobalDef.MS_SECOND;
        }),
    GAP_TEN_MINUTE("10分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            int curmin = cr.get(Calendar.MINUTE);
            return (0 == curmin % 10) && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec);
        },
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return (60 - ((long)cursec % 60)) * 1000;
        }),
    GAP_THIRTY_MINUTE("30分钟",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            int curmin = cr.get(Calendar.MINUTE);
            return (0 == curmin % 30) && (GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec);
        },
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return (60 - ((long)cursec % 60)) * 1000;
        }),

    GAP_ONE_HOUR("1小时",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        },
        cr -> {
            int curmin = cr.get(Calendar.MINUTE);
            return (60 - ((long)curmin % 60)) * GlobalDef.MS_MINUTE;
        }),
    GAP_TWO_HOUR("2小时",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        },
        cr -> {
            int curmin = cr.get(Calendar.MINUTE);
            return (60 - ((long)curmin % 60)) * GlobalDef.MS_MINUTE;
        }),
    GAP_FOUR_HOUR("4小时",
        cr -> {
            int cursec = cr.get(Calendar.SECOND);
            return GlobalDef.INT_GLOBALJOB_CHECKPERIOD > cursec % 30;
        },
        cr -> {
            int curmin = cr.get(Calendar.MINUTE);
            return (60 - ((long)curmin % 60)) * GlobalDef.MS_MINUTE;
        });

    private String szName;
    private Function<Calendar, Boolean> funArrive;
    private Function<Calendar, Long> funDelay;

    ETimeGap(String name, Function<Calendar, Boolean> funer, Function<Calendar, Long> funde)    {
        szName = name;
        funArrive = funer;
        funDelay = funde;
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
     * @param cr    calendar for current
     * @return  true if arrive;
     */
    public boolean isArrive(Calendar cr)  {
        return funArrive.apply(cr);
    }

    /**
     * get delay time(ms) for next arrive
     * @param cr    calendar for current
     * @return      delay time(ms)
     */
    public long getDelay(Calendar cr)   {
        return funDelay.apply(cr);
    }

    /**
     * get ETimeGap from name
     * @param name  name for timeGap
     * @return      ETimeGap or null
     */
    public static ETimeGap getETimeGap(String name) {
        for(ETimeGap et : ETimeGap.values())    {
            if(et.szName.equals(name))
                return et;
        }

        return null;
    }
}

