package com.wxm.camerajob.data.define;

import android.support.annotation.Nullable;

/**
 * for action
 * Created by ookoo on 2018/2/21.
 */
public enum EAction {
    LOAD_JOB("load_job"),
    LOAD_CAMERA_SETTING("load_camera_setting"),
    LOAD_PHOTO_DIR("load_photo_dir");

    private String szActionName;

    EAction(String szName)  {
        szActionName = szName;
    }

    /**
     * get properties name
     * @return  name
     */
    public String getName()  {
        return szActionName;
    }

    /**
     * use name to get enum object
     * @param szName   action name
     * @return         enum object
     */
    @Nullable
    public static EAction getEAction(String szName) {
        for(EAction en : EAction.values())  {
            if(en.szActionName.equals(szName))
                return en;
        }

        return null;
    }
}
