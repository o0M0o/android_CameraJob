package com.wxm.camerajob.ui.extend.scaleLayout

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.wxm.camerajob.R
import wxm.androidutil.improve.let1


/**
 * @author      WangXM
 * @version     create：2018/7/31
 */
class ScaleLayout : ConstraintLayout {
    private var mWidthToHeight = DEF_WIDTH_TO_HEIGHT

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.getDefaultSize(0, widthMeasureSpec),
                (View.getDefaultSize(0, widthMeasureSpec) * mWidthToHeight).toInt())

        // Children are just made to fill our space.
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(measuredHeight, View.MeasureSpec.EXACTLY))
    }

    @SuppressLint("Recycle")
    private fun init(@Suppress("UNUSED_PARAMETER") ct: Context, attrs: AttributeSet?)  {
        context.obtainStyledAttributes(attrs, R.styleable.ScaleLayout)?.let1 {
            mWidthToHeight = it.getFloat(R.styleable.ScaleLayout_slWidthToHeight, DEF_WIDTH_TO_HEIGHT)
            it.recycle()
        }
    }

    companion object {
        private const val DEF_WIDTH_TO_HEIGHT = 1.0f
    }
}