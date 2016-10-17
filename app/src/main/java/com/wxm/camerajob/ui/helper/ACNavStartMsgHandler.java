package com.wxm.camerajob.ui.helper;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wxm.camerajob.base.data.CameraJob;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.handler.GlobalContext;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.ui.acinterface.ACNavStart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cn.wxm.andriodutillib.util.FileUtil;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * activity msg handler
 * Created by wxm on 2016/8/13.
 */

public class ACNavStartMsgHandler extends Handler {
    private static final String TAG = "ACNavStartMsgHandler";
    private ACNavStart mActivity;
    private ArrayList<HashMap<String, String>> mLVList = new ArrayList<>();

    public ACNavStartMsgHandler(ACNavStart acstart) {
        super();
        mActivity = acstart;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case GlobalDef.MSGWHAT_CAMERAJOB_UPDATE :
            case GlobalDef.MSGWHAT_ACSTART_UPDATEJOBS : {
                Message m = Message.obtain(GlobalContext.getMsgHandlder(),
                        GlobalDef.MSGWHAT_CAMERAJOB_ASKALL);
                m.obj = this;
                m.sendToTarget();
            }
            break;

            case GlobalDef.MSGWHAT_REPLAY :     {
                if(GlobalDef.MSGWHAT_CAMERAJOB_ASKALL == msg.arg1) {
                    load_camerajobs(msg);
                }
            }
            break;

            default:
                Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                break;
        }
    }

    private void load_camerajobs(Message msg) {
        LinkedList<String> dirs = FileUtil.getDirDirs(
                ContextUtil.getInstance().getAppPhotoRootDir(),
                false);
        List<CameraJob> lsjob = UtilFun.cast(msg.obj);
        if(null != lsjob) {
            mLVList.clear();
            for (CameraJob cj : lsjob) {
                alive_camerjob(cj);

                String dir = ContextUtil.getInstance().getCameraJobPhotoDir(cj.get_id());
                if (!UtilFun.StringIsNullOrEmpty(dir))
                    dirs.remove(dir);
            }
        }

        for(String dir : dirs)  {
            died_camerajob(dir);
        }

        mActivity.updateData(mLVList);
    }

    private void died_camerajob(String dir) {
        CameraJob cj = ContextUtil.getInstance().getCameraJobFromPath(dir);
        if(null == cj)
            return;

        String jobname = "任务 : " + cj.getName() + "(已移除)";
        String show  = "可以查看本任务已获取图片\n可以移除本任务占据空间";

        HashMap<String, String> map = new HashMap<>();
        map.put(ACNavStart.STR_ITEM_TITLE, jobname);
        map.put(ACNavStart.STR_ITEM_TEXT, show);
        map.put(ACNavStart.STR_ITEM_ID,  Integer.toString(cj.get_id()));
        map.put(ACNavStart.STR_ITEM_STATUS, GlobalDef.STR_CAMERAJOB_STOP);
        map.put(ACNavStart.STR_ITEM_JOBNAME, cj.getName());
        map.put(ACNavStart.STR_ITEM_TYPE, ACNavStart.DIED_JOB);
        mLVList.add(map);
    }

    private void alive_camerjob(CameraJob cj)     {
        String show = String.format("周期/频度  : %s/%s\n开始时间 : %s\n结束时间 : %s"
                , cj.getType(), cj.getPoint()
                ,UtilFun.TimestampToString(cj.getStarttime())
                ,UtilFun.TimestampToString(cj.getEndtime()));

        String jobname = "任务 : " + cj.getName();
        String status = cj.getStatus().getJob_status().equals(GlobalDef.STR_CAMERAJOB_RUN) ?
                "运行" : "暂停";
        jobname = jobname + "(" + status + ")";
        if(0 != cj.getStatus().getJob_photo_count()) {
            show = String.format("%s\n执行成功%d次\n最后拍摄时间 : %s"
                    , show, cj.getStatus().getJob_photo_count()
                    , UtilFun.TimestampToString(cj.getStatus().getTs()));
        }
        else    {
            show = String.format("%s\n执行成功%d次"
                    , show, cj.getStatus().getJob_photo_count());
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(ACNavStart.STR_ITEM_TITLE, jobname);
        map.put(ACNavStart.STR_ITEM_TEXT, show);
        map.put(ACNavStart.STR_ITEM_ID,  Integer.toString(cj.get_id()));
        map.put(ACNavStart.STR_ITEM_STATUS, cj.getStatus().getJob_status());
        map.put(ACNavStart.STR_ITEM_JOBNAME, cj.getName());
        map.put(ACNavStart.STR_ITEM_TYPE, ACNavStart.ALIVE_JOB);
        mLVList.add(map);
    }
}
