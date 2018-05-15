package com.wxm.camerajob.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager

import com.wxm.camerajob.utility.ContextUtil

/**
 * class paraName：SIMCardInfo<BR></BR>
 * class description：读取Sim卡信息<BR></BR>
 * PS： 必须在加入各种权限 <BR></BR>
 * Date:2012-3-12<BR></BR>
 *
 * @version 1.00
 * @author CODYY)peijiangping
 */
class SIMCardInfo(context: Context) {
    /**
     * TelephonyManager提供设备上获取通讯服务信息的入口。 应用程序可以使用这个类方法确定的电信服务商和国家 以及某些类型的用户访问信息。
     * 应用程序也可以注册一个监听器到电话收状态的变化。不需要直接实例化这个类
     * 使用Context.getSystemService(Context.TELEPHONY_SERVICE)来获取这个类的实例。
     */
    private val telephonyManager: TelephonyManager?

    /**
     * Role:获取当前设置的电话号码
     * <BR></BR>Date:2012-3-12
     * <BR></BR>@author CODYY)peijiangping
     */
    // TODO: Consider calling
    //    ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
    //                                          int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
    val nativePhoneNumber: String
        get() {
            val ct = ContextUtil.instance
            if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ct, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return ""
            }
            return if (null == telephonyManager) "" else telephonyManager.line1Number
        }

    /**
     * Role:Telecom service providers获取手机服务商信息 <BR></BR>
     * 需要加入权限<uses-permission android:paraName="android.permission.READ_PHONE_STATE"></uses-permission> <BR></BR>
     * Date:2012-3-12 <BR></BR>
     *
     * @author CODYY)peijiangping
     */
    // TODO: Consider calling
    //    ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
    //                                          int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
    // 返回唯一的用户ID;就是这张卡的编号神马的
    // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
    val providersName: String?
        get() {
            val ct = ContextUtil.instance
            if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return ""
            }

            var ProvidersName: String? = null
            val IMSI = telephonyManager!!.subscriberId
            println(IMSI)
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                ProvidersName = "中国移动"
            } else if (IMSI.startsWith("46001")) {
                ProvidersName = "中国联通"
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "中国电信"
            }
            return ProvidersName
        }

    init {
        telephonyManager = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
}
