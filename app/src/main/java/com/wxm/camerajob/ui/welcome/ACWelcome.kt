package com.wxm.camerajob.ui.welcome

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
import com.wxm.camerajob.BuildConfig
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.ui.help.ACHelp
import com.wxm.camerajob.ui.dialog.DlgUsrMessage
import com.wxm.camerajob.ui.test.camera.ACTestCamera
import com.wxm.camerajob.ui.test.silentCamera.ACTestSilentCamera
import com.wxm.camerajob.utility.AppUtil
import kotterknife.bindView
import wxm.androidutil.improve.let1
import wxm.androidutil.ui.dialog.DlgOKOrNOBase

/**
 * first page after login
 */
class ACWelcome : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    private val mFRWelcome = FrgWelcome()

    private val mTBNav: Toolbar by bindView(R.id.ac_navw_toolbar)
    private val mDLOuterLayout: DrawerLayout by bindView(R.id.ac_start_outerlayout)
    private val mNVNav: NavigationView by bindView(R.id.start_nav_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_job_show)
        AppUtil.addActivity(this)

        // for navigation view
        setSupportActionBar(mTBNav)
        ActionBarDrawerToggle(this, mDLOuterLayout, mTBNav,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close).apply {
            mDLOuterLayout.addDrawerListener(this)
            syncState()
        }
        mNVNav.setNavigationItemSelectedListener(this)

        if (null == savedInstanceState) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_job_show, mFRWelcome)
            transaction.commit()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig)
        mFRWelcome.reloadUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.TestCamera) {
            menuInflater.inflate(R.menu.acm_welcome, menu)
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mFRWelcome.reloadUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_camera_test -> {
                startActivityForResult(Intent(this, ACTestCamera::class.java), 1)
            }

            R.id.mi_silentcamera_test -> {
                startActivityForResult(Intent(this, ACTestSilentCamera::class.java), 1)
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
        DlgUsrMessage().let1 {
            it.addDialogListener(object : DlgOKOrNOBase.DialogResultListener {
                override fun onDialogPositiveResult(dialogFragment: DialogFragment) {}
                override fun onDialogNegativeResult(dialogFragment: DialogFragment) {}
            })

            it.show(supportFragmentManager, "send message")
        }
    }
}
