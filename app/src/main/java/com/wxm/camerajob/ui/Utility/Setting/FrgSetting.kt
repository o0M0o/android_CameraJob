package com.wxm.camerajob.ui.Utility.Setting

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.wxm.camerajob.R

import butterknife.BindView
import butterknife.ButterKnife
import wxm.androidutil.FrgUtility.FrgUtilityBase
import wxm.androidutil.util.UtilFun

/**
 * fragment for setting
 */
class FrgSetting : FrgUtilityBase() {

    @BindView(R.id.vp_pages)
    internal var mVPPage: ViewPager? = null

    /**
     * 得到当前页
     * @return  当前页实例
     */
    val currentPage: TFSettingBase?
        get() {
            val pa = UtilFun.cast_t<PagerAdapter>(mVPPage!!.adapter)
            return UtilFun.cast_t<TFSettingBase>(pa.getItem(mVPPage!!.currentItem))
        }

    var currentItem: Int
        get() = mVPPage!!.currentItem
        set(idx) {
            mVPPage!!.currentItem = idx
        }

    override fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "FrgHelp"
        val rootView = inflater.inflate(R.layout.vw_setting, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    override fun initUiComponent(view: View) {
        val a_ac = UtilFun.cast_t<AppCompatActivity>(activity)

        // for pages
        val adapter = PagerAdapter(a_ac.supportFragmentManager, PAGE_COUNT)
        mVPPage!!.adapter = adapter
    }

    override fun loadUI() {
        change_page(PAGE_IDX_MAIN)
    }

    /**
     * 切换到新页面
     * @param new_page 新页面postion
     */
    fun change_page(new_page: Int) {
        currentItem = new_page
    }


    /**
     * fragment adapter
     */
    inner class PagerAdapter internal constructor(fm: FragmentManager, internal var mNumOfFrags: Int) : FragmentStatePagerAdapter(fm) {
        private val mFRFrags: Array<Fragment>

        init {

            mFRFrags = arrayOfNulls(mNumOfFrags)
            mFRFrags[PAGE_IDX_MAIN] = TFSettingMain()
            mFRFrags[PAGE_IDX_CHECK_VERSION] = TFSettingCheckVersion()
            mFRFrags[PAGE_IDX_DIRECTORY] = TFSettingDirectory()
            mFRFrags[PAGE_IDX_CAMERA] = TFSettingCamera()
        }

        override fun getItem(position: Int): android.support.v4.app.Fragment {
            return mFRFrags[position]
        }

        override fun getCount(): Int {
            return mNumOfFrags
        }
    }

    companion object {
        private val PAGE_COUNT = 4
        val PAGE_IDX_MAIN = 0
        val PAGE_IDX_CHECK_VERSION = 1
        val PAGE_IDX_DIRECTORY = 2
        val PAGE_IDX_CAMERA = 3
    }
}
