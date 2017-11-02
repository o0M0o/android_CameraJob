package com.wxm.camerajob.ui.Utility.Setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wxm.camerajob.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import wxm.androidutil.FrgUtility.FrgUtilityBase;
import wxm.androidutil.util.UtilFun;

/**
 * fragment for setting
 */
public class FrgSetting
        extends FrgUtilityBase {
    private final static int   PAGE_COUNT              = 4;
    public final static int    PAGE_IDX_MAIN           = 0;
    public final static int    PAGE_IDX_CHECK_VERSION  = 1;
    public final static int    PAGE_IDX_DIRECTORY      = 2;
    public final static int    PAGE_IDX_CAMERA         = 3;

    @BindView(R.id.vp_pages)
    ViewPager mVPPage;

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "FrgHelp";
        View rootView = inflater.inflate(R.layout.vw_setting, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {
        AppCompatActivity a_ac = UtilFun.cast_t(getActivity());

        // for pages
        final PagerAdapter adapter = new PagerAdapter
                (a_ac.getSupportFragmentManager(), PAGE_COUNT);
        mVPPage.setAdapter(adapter);
    }

    @Override
    protected void loadUI() {
        change_page(PAGE_IDX_MAIN);
    }

    /**
     * 切换到新页面
     * @param new_page 新页面postion
     */
    public void change_page(int new_page)  {
        setCurrentItem(new_page);
    }

    /**
     *  得到当前页
     * @return  当前页实例
     */
    public TFSettingBase getCurrentPage()   {
        PagerAdapter pa = UtilFun.cast_t(mVPPage.getAdapter());
        return UtilFun.cast_t(pa.getItem(mVPPage.getCurrentItem()));
    }

    public int getCurrentItem() {
        return mVPPage.getCurrentItem();
    }

    public void setCurrentItem(int idx) {
        mVPPage.setCurrentItem(idx);
    }


    /**
     * fragment adapter
     */
    public class PagerAdapter extends FragmentStatePagerAdapter {
        int                 mNumOfFrags;
        private Fragment[]  mFRFrags;

        PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            mNumOfFrags = NumOfTabs;

            mFRFrags = new Fragment[mNumOfFrags];
            mFRFrags[PAGE_IDX_MAIN]             = new TFSettingMain();
            mFRFrags[PAGE_IDX_CHECK_VERSION]    = new TFSettingCheckVersion();
            mFRFrags[PAGE_IDX_DIRECTORY]        = new TFSettingDirectory();
            mFRFrags[PAGE_IDX_CAMERA]           = new TFSettingCamera();
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return mFRFrags[position];
        }

        @Override
        public int getCount() {
            return mNumOfFrags;
        }
    }
}
