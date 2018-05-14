package com.wxm.camerajob.ui.Utility.Help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef

import butterknife.BindView
import butterknife.ButterKnife
import wxm.androidutil.FrgUtility.FrgUtilityBase
import wxm.androidutil.util.UtilFun

/**
 * fragment for help
 */
class FrgHelp : FrgUtilityBase() {

    @BindView(R.id.ac_help_webvw)
    internal var mWVHelp: WebView? = null

    protected fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "FrgHelp"
        val rootView = inflater.inflate(R.layout.frg_help, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    protected fun initUiComponent(view: View) {
        mWVHelp!!.settings.defaultTextEncodingName = ENCODING

        val arg = getArguments()
        if (null != arg) {
            val h_t = arg!!.getString(GlobalDef.STR_HELP_TYPE)
            if (!UtilFun.StringIsNullOrEmpty(h_t)) {
                when (h_t) {
                    GlobalDef.STR_HELP_MAIN -> {
                        load_main_help()
                    }
                }
            }
        }
    }

    protected fun loadUI() {}


    /**
     * 加载应用主帮助信息
     */
    private fun load_main_help() {
        val HELP_MAIN_FILEPATH = "file:///android_asset/help_main.html"
        mWVHelp!!.loadUrl(HELP_MAIN_FILEPATH)
    }

    companion object {
        private val ENCODING = "utf-8"
    }
}
