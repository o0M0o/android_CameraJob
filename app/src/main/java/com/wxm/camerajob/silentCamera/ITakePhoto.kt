package com.wxm.camerajob.silentCamera

/**
 * [TakePhotoParam] is parameter for take photo
 * [ITakePhoto] is listen take photo result
 * @author      WangXM
 * @version     create：2018/12/9
 */

data class TakePhotoParam(val mPhotoFileDir: String,
                          val mFileName: String,
                          val mTag: String)

interface ITakePhoto {
    fun onTakePhotoSuccess(tp: TakePhotoParam)
    fun onTakePhotoFailed(tp: TakePhotoParam)
}