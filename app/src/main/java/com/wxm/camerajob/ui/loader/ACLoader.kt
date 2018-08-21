package com.wxm.camerajob.ui.loader

import android.content.Intent
import android.content.pm.PackageManager
import com.wxm.camerajob.R
import com.wxm.camerajob.ui.welcome.ACWelcome
import com.wxm.camerajob.utility.AppUtil
import wxm.androidutil.ui.activity.ACSwitcherActivity

/**
 * first activity for app
 * apply permission then jump to fist work activity
 */
class ACLoader : ACSwitcherActivity<FrgLoader>()    {
    /**
     * Callback for request permissions
     * if have all permissions jump to work ui
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_ALL) {
            if(0 == grantResults.count{ it != PackageManager.PERMISSION_GRANTED})   {
                jumpWorkActivity()
            } else  {
                hotFragment.reloadUI()
            }
        }
    }

    /**
     * jump to work ui
     */
    fun jumpWorkActivity() {
        AppUtil.initUtil()
        AppUtil.addActivity(this)

        startActivityForResult(Intent(this, ACWelcome::class.java), 1)
    }

    companion object {
        const val REQUEST_ALL = 99
    }
}
