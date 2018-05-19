package com.wxm.camerajob.ui.Base

import android.support.annotation.IdRes
import android.view.View

/**
 * @author      WangXM
 * @version     create：2018/4/24
 */
@Suppress("MemberVisibilityCanBePrivate")
object EventHelper {
    /**
     * set on-click-listener [listener] for views in [vwChildId] with parent view [vwParent]
     */
    fun setOnClickListener(vwParent: View, @IdRes vwChildId: IntArray, listener: View.OnClickListener)     {
        for(@IdRes id in vwChildId) {
            val v:View = vwParent.findViewById(id)
            v.setOnClickListener(listener)
        }
    }

    /**
     * set on-click-function [funOperator] for views in [vwChildId] with parent view [vwParent]
     */
    fun setOnClickOperator(vwParent: View, @IdRes vwChildId: IntArray, funOperator: (v:View) -> Unit)     {
        setOnClickListener(vwParent, vwChildId, View.OnClickListener { v -> funOperator(v) })
    }

    /**
     * set on-click-function [funOperator] for view in [vwObj]
     */
    fun setOnClickFun(vwObj: Array<View>, funOperator: (v:View) -> Unit)    {
        vwObj.forEach   {
            it.setOnClickListener(funOperator)
        }
    }
}