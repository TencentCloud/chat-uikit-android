package com.tencent.qcloud.tuikit.tuicallkit.view.component.inviteuser

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.tencent.qcloud.tuikit.tuicallkit.R
import com.tencent.qcloud.tuikit.tuicallkit.common.data.Logger
import io.trtc.tuikit.atomicxcore.api.call.CallStore

@SuppressLint("AppCompatCustomView")
class InviteUserButton(context: Context) : ImageView(context) {
    private val context: Context = context.applicationContext

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initView()
    }

    private fun initView() {
        this.setBackgroundResource(R.drawable.tuicallkit_ic_add_user_black)
        this.setOnClickListener {
            inviteUser()
        }
        layoutParams = layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        visibility = VISIBLE
    }

    private fun inviteUser() {
        // TODO: 3.0 上这里不止是groupId
        val groupId = CallStore.shared.observerState.activeCall.value.chatGroupId
        if (groupId.isNullOrEmpty()) {
            ToastUtil.toastShortMessage(context.getString(R.string.callkit_group_id_is_empty))
            return
        }
        val list = ArrayList<String>()
        val allParticipants = CallStore.shared.observerState.allParticipants.value
        for (participant in allParticipants) {
            list.add(participant.id)
        }
        Logger.i(TAG, "InviteUserButton clicked, groupId: $groupId ,list: $list")
        val bundle = Bundle()
        bundle.putString("groupId", groupId)
        bundle.putStringArrayList("selectMemberList", list)
        TUICore.startActivity("SelectGroupMemberActivity", bundle)
    }

    companion object {
        private const val TAG = "InviteUserButton"
    }
}