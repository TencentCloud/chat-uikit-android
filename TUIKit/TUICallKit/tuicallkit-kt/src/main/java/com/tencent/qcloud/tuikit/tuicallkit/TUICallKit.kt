package com.tencent.qcloud.tuikit.tuicallkit

import android.content.Context
import com.tencent.cloud.tuikit.engine.call.TUICallDefine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine.RoomId
import com.tencent.qcloud.tuikit.tuicallkit.common.data.Constants
import com.tencent.qcloud.tuikit.tuicallkit.view.CallAdapter
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.call.CallMediaType
import io.trtc.tuikit.atomicxcore.api.call.CallParams

abstract class TUICallKit {
    companion object {
        @JvmStatic
        fun createInstance(context: Context): TUICallKit = TUICallKitImpl.createInstance(context)
    }

    /**
     * Set user profile
     *
     * @param nickname User name, which can contain up to 500 bytes
     * @param avatar   User profile photo URL, which can contain up to 500 bytes
     * For example: https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar1.png
     * @param callback Set the result callback
     */
    open fun setSelfInfo(nickname: String?, avatar: String?, completion: CompletionHandler?) {}

    /**
     * Make a 1VN calls
     *
     * @param userIdList    List of userId
     * @param mediaType     Call type
     * @param params        Extension param: eg: offlinePushInfo
     */
    open fun calls(
        userIdList: List<String>, mediaType: CallMediaType,
        params: CallParams?, completion: CompletionHandler?
    ) {
    }

    /**
     * Join a current call
     *
     * @param callId call Id
     */
    open fun join(callId: String?, completion: CompletionHandler?) {}


    /**
     * Set the ringtone (preferably shorter than 30s)
     *
     * @param filePath Callee ringtone path
     */
    open fun setCallingBell(filePath: String?) {}

    /**
     * Enable the mute mode (the callee doesn't ring)
     */
    open fun enableMuteMode(enable: Boolean) {}

    /**
     * Enable the floating window
     */
    open fun enableFloatWindow(enable: Boolean) {}

    /**
     * Enable Virtual Background
     */
    open fun enableVirtualBackground(enable: Boolean) {}

    open fun enableAITranscriber(enable: Boolean) {}

    /**
     * Enable callee show banner view when received an new invitation
     * default: false
     */
    open fun enableIncomingBanner(enable: Boolean) {}

    /**
     * Set the display direction of the CallKit interface. The default value is portrait
     * @param orientation:  0-Portrait, 1-LandScape, 2-Auto;   default value: 0
     * Note: You are advised to use portrait mode to avoid abnormal display for small screen devices such as mobile phone
     */
    open fun setScreenOrientation(orientation: Int) {}

    /**
     * Disable the control button
     *
     * @param button value: microphone, audioDevice, camera, switchCamera, inviteUser.
     */
    open fun disableControlButton(button: Constants.ControlButton?) {}

    /**
     * set call adapter to customize call ui
     */
    open fun setAdapter(adapter: CallAdapter?) {}

    /**
     * Call experimental API
     *
     * @param jsonStr
     */
    open fun callExperimentalAPI(jsonStr: String) {}

    /**
     * Join a current call
     *
     * @param roomId        current call room ID
     * @param callMediaType call type
     */
    @Deprecated("Use NewInterface instead", ReplaceWith("join"))
    open fun joinInGroupCall(roomId: RoomId?, groupId: String?, callMediaType: TUICallDefine.MediaType?) {
    }

}