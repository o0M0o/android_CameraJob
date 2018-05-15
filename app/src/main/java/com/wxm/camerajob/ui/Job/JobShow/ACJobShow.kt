package com.wxm.camerajob.ui.Job.JobShow

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import com.wxm.camerajob.BuildConfig
import com.wxm.camerajob.data.define.EAction
import com.wxm.camerajob.ui.Job.JobCreate.ACJobCreate
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.GlobalDef
import com.wxm.camerajob.ui.dialog.DlgUsrMessage
import com.wxm.camerajob.utility.CameraJobUtility
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.utility.FileLogger
import com.wxm.camerajob.ui.Utility.Help.ACHelp
import com.wxm.camerajob.ui.Camera.CameraSetting.ACCameraSetting
import com.wxm.camerajob.ui.Utility.Setting.ACSetting
import com.wxm.camerajob.ui.Test.Camera.ACCameraTest
import com.wxm.camerajob.ui.Test.Camera.ACSilentCameraTest

import butterknife.BindView
import butterknife.ButterKnife
import wxm.androidutil.Dialog.DlgOKOrNOBase
import wxm.androidutil.util.UtilFun

/**
 * UI(main UI for app) for show job status
 */
class ACJobShow : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val mFRGJobShow = FrgJobShow()

    @BindView(R.id.ac_navw_toolbar)
    internal var mTBNavw: Toolbar? = null

    @BindView(R.id.ac_start_outerlayout)
    internal var mDLOuterLayout: DrawerLayout? = null

    @BindView(R.id.start_nav_view)
    internal var mNVNav: NavigationView? = null

    /**
     * 如果有权限，则直接初始化实例
     * 如果无权限，则申请权限后，初始化实例
     * @param savedInstanceState   param
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_job_show)
        ContextUtil.instance.addActivity(this)

        ButterKnife.bind(this)
        initActivity()
        if (null == savedInstanceState) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fl_job_show, mFRGJobShow)
            transaction.commit()
        }
    }

    private fun initActivity() {
        // set nav view
        try {
            setSupportActionBar(mTBNavw)

            val toggle = ActionBarDrawerToggle(
                    this, mDLOuterLayout, mTBNavw,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close)
            mDLOuterLayout!!.addDrawerListener(toggle)
            toggle.syncState()

            mNVNav!!.setNavigationItemSelectedListener(this)
        } catch (e: NullPointerException) {
            FileLogger.logger.severe(UtilFun.ThrowableToString(e))
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig)
        mFRGJobShow.refreshFrg()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.acm_job_show, menu)

        if (!BuildConfig.TestCamera) {
            var mi = menu.findItem(R.id.mi_camera_test)
            mi.isVisible = false

            mi = menu.findItem(R.id.mi_silentcamera_test)
            mi.isVisible = false
        }

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_job_add -> {
                val intent = Intent(this, ACJobCreate::class.java)
                startActivityForResult(intent, 1)
            }

            R.id.mi_setting -> {
                val data = Intent(this, ACSetting::class.java)
                startActivityForResult(data, 1)
            }

            R.id.mi_camera_setting -> {
                val data = Intent(this, ACCameraSetting::class.java)
                startActivityForResult(data, 1)
            }

            R.id.mi_camera_test -> {
                val data = Intent(this, ACCameraTest::class.java)
                startActivityForResult(data, 1)
            }

            R.id.mi_silentcamera_test -> {
                run {
                    val data = Intent(this, ACSilentCameraTest::class.java)
                    startActivityForResult(data, 1)
                }
                return super.onOptionsItemSelected(item)
            }

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            GlobalDef.INTRET_CAMERAJOB_ACCEPT -> {
                val cj = data.getParcelableExtra<CameraJob>(EAction.LOAD_JOB.actName)
                Log.d(TAG, "camerajob : " + cj.toString())

                CameraJobUtility.createCameraJob(cj)
            }

            GlobalDef.INTRET_CS_ACCEPT -> {
                /*Message m = Message.obtain(GlobalContext.getMsgHandler(),
                        GlobalDef.MSG_TYPE_CAMERA_MODIFY);
                m.obj = PreferencesUtil.loadCameraParam();
                m.sendToTarget(); */
            }

            else -> {
                Log.v(TAG, "not match resultCode = $resultCode")
            }
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
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

        mDLOuterLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * 给作者留言
     */
    private fun contactWriter() {
        val dlg = DlgUsrMessage()
        dlg.addDialogListener(object : DlgOKOrNOBase.DialogResultListener {
            override fun onDialogPositiveResult(dialogFragment: DialogFragment) {}

            override fun onDialogNegativeResult(dialogFragment: DialogFragment) {}
        })

        dlg.show(supportFragmentManager, "send message")
    }

    companion object {
        private val TAG = "ACJobShow"
    }
}
