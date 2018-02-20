package com.wxm.camerajob.data.define;

/**
 * msg type
 * Created by ookoo on 2018/2/19.
 */
public enum EMsgType {
    WAKEUP("wakeup", 1000),

    CAMERAJOB_QUERY("query camera job", 1102),
    CAMERAJOB_MODIFY("modify camera job", 1103),
    CAMERAJOB_TAKEPHOTO("camera job take photo", 1104),

    JOBSHOW_UPDATE("update job show", 1200),
    REPLAY("replay", 9000);

    private String szType;
    private int iId;

    EMsgType(String type, int id)    {
        szType = type;
        iId = id;
    }

    /**
     * get type name
     * @return  type name
     */
    public String getType()   {
        return szType;
    }

    /**
     * get type id
     * @return  type id
     */
    public int getId()  {
        return iId;
    }

    /**
     * get EMsgType from id
     * @param id    id for msg type
     * @return      EMsgType or null
     */
    public static EMsgType getEMsgType(int id) {
        for(EMsgType et : EMsgType.values())    {
            if(et.getId() == id)
                return et;
        }

        return null;
    }
}
