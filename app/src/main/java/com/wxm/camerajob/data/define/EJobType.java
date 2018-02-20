package com.wxm.camerajob.data.define;

/**
 * job type
 * Created by ookoo on 2018/2/20.
 */
public enum EJobType {
    JOB_MINUTELY("每分钟"),
    JOB_HOURLY("每小时"),
    JOB_DAILY("每天");

    private String szType;

    EJobType(String type)    {
        szType = type;
    }

    /**
     * get type name
     * @return  type name
     */
    public String getType()   {
        return szType;
    }

    /**
     * get EJobType from name
     * @param ty    name for job type
     * @return      EJobType or null
     */
    public static EJobType getEJobType(String ty) {
        for(EJobType et : EJobType.values())    {
            if(et.szType.equals(ty))
                return et;
        }

        return null;
    }
}
