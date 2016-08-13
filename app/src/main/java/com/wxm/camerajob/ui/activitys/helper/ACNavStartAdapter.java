package com.wxm.camerajob.ui.activitys.helper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.UtilFun;
import com.wxm.camerajob.ui.activitys.ActivityNavStart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * activity adapter
 * Created by wxm on 2016/8/13.
 */
public class ACNavStartAdapter extends SimpleAdapter {
    private ActivityNavStart mHome;
    private ArrayList<HashMap<String, String>> mLVList;

    public ACNavStartAdapter(ActivityNavStart home,
                           Context context, List<? extends Map<String, ?>> data,
                           String[] from,
                           int[] to) {
        super(context, data, R.layout.listitem_jobstatus, from, to);
        mHome = home;
        mLVList = UtilFun.cast(data);
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        View v = super.getView(position, view, arg2);
        if(null != v)   {
            ImageButton ib_play = (ImageButton)v.findViewById(R.id.liib_jobstatus_run_pause);
            ImageButton ib_delete = (ImageButton)v.findViewById(R.id.liib_jobstatus_stop);
            ImageButton ib_look = (ImageButton)v.findViewById(R.id.liib_jobstatus_look);

            HashMap<String, String> map = mLVList.get(position);
            String status = map.get(ActivityNavStart.STR_ITEM_STATUS);
            if(status.equals(GlobalDef.STR_CAMERAJOB_RUN))  {
                ib_play.setVisibility(View.VISIBLE);

                ib_play.setBackgroundResource(android.R.drawable.ic_media_pause);
                ib_play.setClickable(true);
                ib_play.setOnClickListener(mHome);
            } else if(status.equals(GlobalDef.STR_CAMERAJOB_PAUSE))     {
                ib_play.setVisibility(View.VISIBLE);

                ib_play.setBackgroundResource(android.R.drawable.ic_media_play);
                ib_play.setClickable(true);
                ib_play.setOnClickListener(mHome);
            } else  {
                ib_play.setVisibility(View.INVISIBLE);
                ib_play.setClickable(false);
            }

            ib_delete.setOnClickListener(mHome);
            ib_look.setOnClickListener(mHome);
        }

        return v;
    }
}
