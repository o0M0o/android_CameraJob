package com.wxm.camerajob.data.define;

import android.support.annotation.Nullable;

/**
 * for properties
 * Created by ookoo on 2018/2/21.
 */
public enum EProperty {
    PROPERTIES_FILENAME("appConfig.properties"),
    PROPERTIES_CAMERA_FACE("camera_face"),
    PROPERTIES_CAMERA_DPI("camera_dpi"),
    PROPERTIES_CAMERA_AUTO_FLASH("camera_auto_flash"),
    PROPERTIES_CAMERA_AUTO_FOCUS("camera_auto_focus");

    private String szPropertiesName;

    EProperty(String szName)  {
        szPropertiesName = szName;
    }

    /**
     * get properties name
     * @return  name
     */
    public String getName()  {
        return szPropertiesName;
    }

    /**
     * use name to get enum object
     * @param szName    property name
     * @return      enum object
     */
    @Nullable
    public static EProperty getEPropertyName(String szName) {
        for(EProperty en : EProperty.values())  {
            if(en.szPropertiesName.equals(szName))
                return en;
        }

        return null;
    }
}
