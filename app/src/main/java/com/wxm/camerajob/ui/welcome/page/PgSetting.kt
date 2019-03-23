@file:Suppress("unused")
package com.wxm.camerajob.ui.welcome.page

import android.os.Bundle
import com.wxm.camerajob.R
import com.wxm.camerajob.ui.event.ChangePage
import com.wxm.camerajob.ui.base.PageBase
import com.wxm.camerajob.ui.setting.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.ui.dialog.DlgAlert
import wxm.androidutil.ui.frg.FrgSupportSwitcher

/**
 * fragment for show job
 * Created by WangXM on 2016/10/14.
 */
class PgSetting : FrgSupportSwitcher<TFSettingBase>(), PageBase {
    private val mPGMain = TFSettingMain()
    private val mPGCamera = TFSettingCamera()
    private val mPGDirectory = TFSettingDirectory()
    private val mPGCheckVersion = TFSettingCheckVersion()

    init {
        setupFrgID(R.layout.pg_empty_page, R.id.fl_page)
    }

    override fun isUseEventBus(): Boolean = true

    /**
     * handler for DB data change
     * @param event     for event
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangePageEvent(event: ChangePage) {
        when(event.JavaClassName)   {
            TFSettingCamera::class.java.name ->    {
                switchToPage(mPGCamera)
            }

            TFSettingDirectory::class.java.name ->    {
                switchToPage(mPGDirectory)
            }

            TFSettingCheckVersion::class.java.name ->    {
                switchToPage(mPGCheckVersion)
            }
        }
    }


    override fun setupFragment(savedInstanceState: Bundle?) {
        addChildFrg(mPGMain)
        addChildFrg(mPGCamera)
        addChildFrg(mPGDirectory)
        addChildFrg(mPGCheckVersion)
    }

    override fun leavePage(): Boolean {
        val hp = hotPage
        if(hp !== mPGMain)    {
            if(hp.isSettingDirty) {
                DlgAlert.showAlert(context!!, R.string.dlg_hint, R.string.setting_changed
                ) { db ->
                    db.setPositiveButton(R.string.accept) { _, _ ->
                        hp.updateSetting()
                    }
                }
            }

            switchToPage(mPGMain)
            return false
        }

        return true
    }

    /// BEGIN PRIVATE
    /// END PRIVATE
}

