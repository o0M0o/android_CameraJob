package com.wxm.camerajob.ui.job.show

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.DialogFragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import butterknife.ButterKnife
import com.wxm.camerajob.BuildConfig
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.ui.job.create.ACJobCreate
import com.wxm.camerajob.ui.help.ACHelp
import com.wxm.camerajob.ui.setting.ACSetting
import com.wxm.camerajob.ui.camera.setting.ACCameraSetting
import com.wxm.camerajob.ui.dialog.DlgUsrMessage
import com.wxm.camerajob.ui.test.camera.ACTestCamera
import com.wxm.camerajob.ui.test.silentCamera.ACTestSilentCamera
import com.wxm.camerajob.App
import kotterknife.bindView
import wxm.androidutil.ui.dialog.DlgOKOrNOBase

/**
 * UI(main UI for app) for show job status
 */
class ACJobShow : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val mFRGJobShow = FrgJobShow()

    private val mTBNav: Toolbar by bindView(R.id.ac_navw_toolbar)
    private val mDLOuterLayout: DrawerLayout by bindView(R.id.ac_start_outerlayout)
    private val mNVNav: NavigationView by bindView(R.id.start_nav_view)

    /**
     * 如果有权限，则直接初始化实例
     * 如果无权限，则申请权限后，初始化实例
     * @param savedInstanceState   param
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_job_show)
        App.addActivity(this)

        ButterKnife.bind(this)
        initActivity()
        if (null == savedInstanceState) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_job_show, mFRGJobShow)
            transaction.commit()
        }
    }

    private fun initActivity() {
        setSupportActionBar(mTBNav)

        ActionBarDrawerToggle(this, mDLOuterLayout, mTBNav,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close).apply {
            mDLOuterLayout.addDrawerListener(this)
            syncState()
        }

        mNVNav.setNavigationItemSelectedListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mFRGJobShow.reloadUI()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.acm_job_show, menu)

        @Suppress("ConstantConditionIf")
        if (!BuildConfig.TestCamera) {
            menu.findItem(R.id.mi_silentcamera_test).isVisible = false
            menu.findItem(R.id.mi_camera_test).isVisible = false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mFRGJobShow.reloadUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_job_add -> {
                startActivityForResult(Intent(this, ACJobCreate::class.java), 1)
            }

            R.id.mi_setting -> {
                startActivityForResult(Intent(this, ACSetting::class.java), 1)
            }

            R.id.mi_camera_setting -> {
                startActivityForResult(Intent(this, ACCameraSetting::class.java), 1)
            }

            R.id.mi_camera_test -> {
                startActivityForResult(Intent(this, ACTestCamera::class.java), 1)
            }

            R.id.mi_silentcamera_test -> {
                startActivityForResult(Intent(this, ACTestSilentCamera::class.java), 1)
                return super.onOptionsItemSelected(item)
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_help -> {
                val d = Intent(this, ACHelp::class.java)
                d.putExtra(GlobalDef.STR_HELP_TYPE, GlobalDef.STR_HELP_MAIN)
                startActivityForResult(d, 1)
            }

            R.id.nav_setting -> {
            }

            R.id.nav_share_app -> {
            }

            R.id.nav_contact_writer -> {
                contactWriter()
            }
        }

        mDLOuterLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * 给作者留言
     */
    private fun contactWriter() {
        DlgUsrMessage().let {
            it.addDialogListener(object : DlgOKOrNOBase.DialogResultListener {
                override fun onDialogPositiveResult(dialogFragment: DialogFragment) {}
                override fun onDialogNegativeResult(dialogFragment: DialogFragment) {}
            })

            it.show(supportFragmentManager, "send message")
            Unit
        }
    }
}
