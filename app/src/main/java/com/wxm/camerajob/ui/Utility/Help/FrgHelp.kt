package com.wxm.camerajob.ui.Utility.Help

import android.os.Bundle
import wxm.androidutil.FrgWebView.FrgSupportWebView

/**
 * fragment for help
 */
class FrgHelp : FrgSupportWebView() {
    override fun loadUI(savedInstanceState: Bundle?) {
        loadPage("file:///android_asset/help_main.html", null)
    }
}
