package com.wxm.camerajob.ui.base

/**
 * accept or cancel
 * @author      WangXM
 * @version     create：2018/5/18
 */
interface IAcceptAble {
    /**
     * when accept if everything is ok, return true
     */
    fun onAccept(): Boolean

    /**
     * when cancel if everything is ok, return true
     */
    fun onCancel(): Boolean
}