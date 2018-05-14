package com.wxm.camerajob.ui.Base

import com.nostra13.universalimageloader.core.ImageLoader

import cn.finalteam.galleryfinal.PauseOnScrollListener

/**
 * Desction:
 * Author:pengjianbo
 * Date:2016/1/9 0009 18:47
 */
class UILPauseOnScrollListener(pauseOnScroll: Boolean, pauseOnFling: Boolean) : PauseOnScrollListener(pauseOnScroll, pauseOnFling) {

    override fun resume() {
        ImageLoader.getInstance().resume()
    }

    override fun pause() {
        ImageLoader.getInstance().pause()
    }
}
