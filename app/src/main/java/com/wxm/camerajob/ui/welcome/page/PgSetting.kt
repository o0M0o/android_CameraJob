@file:Suppress("unused")
package com.wxm.camerajob.ui.welcome.page

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
@Suppress("BooleanLiteralArgument")
class PgSetting : FrgSupportSwitcher<TFSettingBase>(), PageBase {
    private val mPGMain = TFSettingMain()
    private val mPGCamera = TFSettingCamera()
    private val mPGDirectory = TFSettingDirectory()
    private val mPGCheckVersion = TFSettingCheckVersion()

    init {
        setupFrgID(R.layout.pg_empty_page, R.id.fl_page)
    }

    override fun isUseEventBus(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        return doLeave(true, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.acm_save_giveup, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_save -> {
                doLeave(false, true)
            }

            R.id.mi_giveup -> {
                doLeave(true, false)
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    /// BEGIN PRIVATE
    /**
     * use it leave setting
     * if [changePage] true, switch to mainPage
     * if [doSave] true, save changed setting
     *
     * @return true if current setting page is mainPage
     */
    private fun doLeave(changePage: Boolean, doSave: Boolean): Boolean    {
        val hp = hotPage
        return if(hp !== mPGMain) {
            if (hp.isSettingDirty && doSave) {
                DlgAlert.showAlert(context!!, R.string.dlg_hint, R.string.setting_changed
                ) { db ->
                    db.setPositiveButton(R.string.accept) { _, _ ->
                        hp.updateSetting()
                    }
                }
            }

            if(changePage) {
                switchToPage(mPGMain)
            }

            false
        } else  {
            true
        }
    }
    /// END PRIVATE
}

