package com.wxm.camerajob.data.define;

/**
 * event for preference change
 * Created by User on 2017/2/14.
 */
public class PreferencesChangeEvent {
    private String mPreferencesName;

    public PreferencesChangeEvent(String preference_name)  {
        mPreferencesName = preference_name;
    }

    /**
     * get changed preference name
     * @return  preference name
     */
    public String getPreferencesName()   {
        return mPreferencesName;
    }
}
