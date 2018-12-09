package com.wxm.camerajob.silentCamera

/**
 * use [TakePhotoParam] set take photo parameter
 * use [ITakePhoto] listen take photo result
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