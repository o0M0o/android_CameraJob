package com.wxm.camerajob.ui.Utility.Setting

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager

import com.wxm.camerajob.R

import kotterknife.bindView
import wxm.androidutil.FrgUtility.FrgSupportBaseAdv
import wxm.androidutil.util.UtilFun

/**
 * fragment for setting
 */
class FrgSetting : FrgSupportBaseAdv() {
    private val mVPPage: ViewPager by bindView(R.id.vp_pages)

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.vw_setting

    /**
     * 得到当前页
     * @return  当前页实例
     */
    val currentPage: TFSettingBase?
        get() {
            val pa = UtilFun.cast_t<PagerAdapter>(mVPPage.adapter)
            return UtilFun.cast_t<TFSettingBase>(pa.getItem(mVPPage.currentItem))
        }

    var currentItem: Int
        get() = mVPPage.currentItem
        set(idx) {
            mVPPage.currentItem = idx
        }

    override fun initUI(savedInstanceState: Bundle?) {
        mVPPage.adapter = PagerAdapter(activity.supportFragmentManager)
        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        changePage(PAGE_IDX_MAIN)
    }

    /**
     * 切换到新页面
     * @param new_page 新页面postion
     */
    private fun changePage(new_page: Int) {
        currentItem = new_page
    }

    /**
     * fragment adapter
     */
    inner class PagerAdapter internal constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private val mFRFrags: Array<TFSettingBase> = 
                arrayOf(TFSettingMain(), TFSettingCheckVersion(), TFSettingDirectory(), TFSettingCamera())

        override fun getItem(pos: Int): android.support.v4.app.Fragment {
            return mFRFrags[pos]
        }

        override fun getCount(): Int {
            return mFRFrags.size
        }
    }

    companion object {
        const val PAGE_IDX_MAIN = 0
        const val PAGE_IDX_CHECK_VERSION = 1
        const val PAGE_IDX_DIRECTORY = 2
        const val PAGE_IDX_CAMERA = 3
    }
}
