package com.tencent.qcloud.tuikit.tuicallkit.view.component.recents.interfaces

import android.view.View
import io.trtc.tuikit.atomicxcore.api.call.CallInfo

interface ICallRecordItemListener {
    fun onItemClick(view: View?, viewType: Int, callInfo: CallInfo?)
    fun onItemDeleteClick(view: View?, viewType: Int, callInfo: CallInfo?)
    fun onDetailViewClick(view: View?, callInfo: CallInfo?)
}