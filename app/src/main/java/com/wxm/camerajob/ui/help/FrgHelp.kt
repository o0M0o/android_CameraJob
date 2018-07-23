package com.wxm.camerajob.ui.help

import android.os.Bundle
import wxm.androidutil.ui.frg.FrgSupportWebView

/**
 * fragment for help
 */
class FrgHelp : FrgSupportWebView() {
    override fun loadUI(savedInstanceState: Bundle?) {
        loadPage("file:///android_asset/help_main.html", null)
    }
}
