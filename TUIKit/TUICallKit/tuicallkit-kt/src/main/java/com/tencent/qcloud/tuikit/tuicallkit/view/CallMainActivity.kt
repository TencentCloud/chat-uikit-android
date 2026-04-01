package com.tencent.qcloud.tuikit.tuicallkit.view

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import android.Manifest
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.qcloud.tuicore.permission.PermissionCallback
import com.tencent.qcloud.tuicore.permission.PermissionRequester
import com.tencent.qcloud.tuicore.util.TUIBuild
import com.tencent.qcloud.tuikit.tuicallkit.R
import com.tencent.qcloud.tuikit.tuicallkit.common.data.Constants
import com.tencent.qcloud.tuikit.tuicallkit.common.data.Logger
import com.tencent.qcloud.tuikit.tuicallkit.common.metrics.KeyMetrics
import com.tencent.qcloud.tuikit.tuicallkit.common.utils.DeviceUtils
import com.tencent.qcloud.tuikit.tuicallkit.common.utils.PermissionRequest
import com.tencent.qcloud.tuikit.tuicallkit.manager.CallManager
import com.tencent.qcloud.tuikit.tuicallkit.state.GlobalState
import com.tencent.qcloud.tuikit.tuicallkit.state.ViewState
import com.tencent.qcloud.tuikit.tuicallkit.view.component.inviteuser.InviteUserButton
import com.trtc.tuikit.common.FullScreenActivity
import com.trtc.tuikit.common.imageloader.ImageLoader
import com.trtc.tuikit.common.imageloader.ImageOptions
import com.trtc.tuikit.common.ui.floatwindow.FloatWindowManager
import io.trtc.tuikit.atomicx.callview.CallView
import kotlinx.coroutines.launch
import com.trtc.tuikit.common.util.ToastUtil
import io.trtc.tuikit.atomicx.callview.Feature
import io.trtc.tuikit.atomicxcore.api.call.CallMediaType
import io.trtc.tuikit.atomicxcore.api.call.CallStore
import io.trtc.tuikit.atomicxcore.api.call.CallParticipantStatus
import io.trtc.tuikit.atomicxcore.api.device.DeviceStore
import io.trtc.tuikit.atomicxcore.api.view.CallLayoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class CallMainActivity : FullScreenActivity() {
    private var callView: CallView? = null
    private var imageFloatIcon: ImageView? = null
    private var inviteUserButton: FrameLayout? = null
    private var subscribeStateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceUtils.setScreenLockParams(window)
        if (TUIBuild.getVersionInt() >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        setContentView(R.layout.tuicallkit_activity_call_kit)
        requestedOrientation = when (GlobalState.instance.orientation) {
            Constants.Orientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Constants.Orientation.LandScape -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        subscribeStateJob = CoroutineScope(Dispatchers.Main).launch {
            observeSelfInfo()
        }
        val mediaType = CallStore.shared.observerState.activeCall.value.mediaType
        if (mediaType != null) {
            setAudioDeviceRoute(mediaType)
            openDeviceMediaForMediaType(mediaType)
        }
        CallManager.instance.startForegroundService()
    }

    private fun initView() {
        val callStatus = CallStore.shared.observerState.selfInfo.value.status
        if (CallParticipantStatus.None == callStatus) {
            finishCallMainActivity()
            return
        }
        val callId = CallStore.shared.observerState.activeCall.value.callId
        KeyMetrics.countUV(KeyMetrics.EventId.WAKEUP, callId)
        setBackground()
        addCallView()
        addFloatButton()
        addInviteButton()
        FloatWindowManager.sharedInstance().dismiss()
        CallManager.instance.viewState.router.set(ViewState.ViewRouter.FullView)
    }

    private suspend fun observeSelfInfo() {
        CallStore.shared.observerState.selfInfo.collect { selfInfo ->
            if (selfInfo.status == CallParticipantStatus.None) {
                finishCallMainActivity()
                return@collect
            }
        }
    }

    private fun finishCallMainActivity() {
        callView?.removeAllViews()
        if (TUIBuild.getVersionInt() >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            finish()
        }
    }

    private fun addCallView() {
        val callViewContainer = findViewById<FrameLayout>(R.id.call_view_container)
        callViewContainer?.removeAllViews()
        callView?.removeAllViews()
        callView = CallView(this)
        val chatGroupId = CallStore.shared.observerState.activeCall.value.chatGroupId
        val inviteeIdListSize = CallStore.shared.observerState.activeCall.value.inviteeIds.size
        if (inviteeIdListSize == 1 && chatGroupId.isEmpty()) {
            callView?.setLayoutTemplate(CallLayoutTemplate.Float)
        } else {
            callView?.setLayoutTemplate(CallLayoutTemplate.Grid)
        }
        if (!GlobalState.instance.enableAITranscriber) {
            callView?.disableFeatures(listOf(Feature.AI_TRANSCRIBER))
        }
        val view = GlobalState.instance.callAdapter?.onCreateMainView(callView!!) ?: callView
        callViewContainer?.addView(view)
    }

    private fun addFloatButton() {
        imageFloatIcon = findViewById(R.id.image_float_icon)
        imageFloatIcon?.setOnClickListener {
            if (FloatWindowManager.sharedInstance().isPictureInPictureSupported) {
                enterPictureInPictureModeWithBuild()
            }
        }
    }

    private fun addInviteButton() {
        inviteUserButton = findViewById(R.id.rl_layout_invite_user)
        if (shouldShowInviteButton()) {
            inviteUserButton?.visibility = View.VISIBLE
            inviteUserButton?.addView(InviteUserButton(this))
            return
        }
        inviteUserButton?.visibility = View.GONE
    }

    private fun shouldShowInviteButton(): Boolean {
        val chatGroupId = CallStore.shared.observerState.activeCall.value.chatGroupId
        return !chatGroupId.isNullOrEmpty()
    }

    private fun setBackground() {
        val imageBackground = findViewById<ImageView>(R.id.img_view_background)
        val selfUser = CallStore.shared.observerState.selfInfo.value
        val option = ImageOptions.Builder().setPlaceImage(R.drawable.tuicallkit_ic_avatar).setBlurEffect(80f).build()
        ImageLoader.load(this, imageBackground, selfUser.avatarUrl, option)
        imageBackground?.setColorFilter(ContextCompat.getColor(this, R.color.callkit_color_blur_mask))
    }

    override fun onResume() {
        super.onResume()
        val mediaType = CallStore.shared.observerState.activeCall.value.mediaType
        PermissionRequest.requestPermissions(application, mediaType,
            object : PermissionCallback() {
                override fun onGranted() {
                    initView()
                }

                override fun onDenied() {
                    val self = CallStore.shared.observerState.selfInfo.value
                    if (!isCaller(self.id)) {
                        CallManager.instance.reject(null)
                    }
                    finishCallMainActivity()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        subscribeStateJob?.cancel()
        CallManager.instance.stopForegroundService()
        Logger.i(TAG, "onDestroy")
    }

    override fun onUserLeaveHint() {
        val hasAudioPermission = PermissionRequester.newInstance(Manifest.permission.RECORD_AUDIO).has()
        if (!hasAudioPermission) {
            return
        }
        val hasVideoPermission = PermissionRequester.newInstance(Manifest.permission.CAMERA).has()
        val mediaType = CallStore.shared.observerState.activeCall.value.mediaType
        if (mediaType == CallMediaType.Video && !hasVideoPermission) {
            return
        }
        enterPictureInPictureModeWithBuild()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        imageFloatIcon?.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
        if (shouldShowInviteButton()) {
            inviteUserButton?.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
        }
        if (isInPictureInPictureMode) {
            callView?.setLayoutTemplate(CallLayoutTemplate.Pip)
        } else {
            hangupOnPipWindowClose()
        }
    }

    private fun hangupOnPipWindowClose() {
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            Logger.i(TAG, "user close pip window")
            CallManager.instance.hangup(null)
        }
    }

    private fun enterPictureInPictureModeWithBuild() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            return
        }
        if (TUIBuild.getVersionInt() >= Build.VERSION_CODES.O && hasPipModePermission()) {
            val pictureInPictureParams: PictureInPictureParams.Builder = PictureInPictureParams.Builder()
            val floatViewWidth = resources.getDimensionPixelSize(R.dimen.callkit_video_small_view_width)
            val floatViewHeight = resources.getDimensionPixelSize(R.dimen.callkit_video_small_view_height)
            val aspectRatio = Rational(floatViewWidth, floatViewHeight)
            pictureInPictureParams.setAspectRatio(aspectRatio).build()
            val requestPipSuccess = this.enterPictureInPictureMode(pictureInPictureParams.build())
            if (!requestPipSuccess) {
                CallManager.instance.viewState.enterPipMode.set(false)
                return
            }
        } else {
            Logger.w(TAG, "current version (" + Build.VERSION.SDK_INT + ") does not support picture-in-picture")
        }
    }

    private fun hasPipModePermission(): Boolean {
        val appOpsManager = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val hasPipModePermission =
            (AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                this.applicationInfo.uid,
                this.packageName
            ))
        if (!hasPipModePermission) {
            ToastUtil.toastShortMessage(getString(R.string.callkit_enter_pip_mode_fail_hint))
        }
        return hasPipModePermission
    }

    private fun openDeviceMediaForMediaType(mediaType: CallMediaType) {
        DeviceStore.shared().openLocalMicrophone(null)
        if (mediaType == CallMediaType.Video) {
            DeviceStore.shared().openLocalCamera(true, null)
        }
    }

    private fun setAudioDeviceRoute(mediaType: CallMediaType) {
        if (CallMediaType.Video == mediaType) {
            CallManager.instance.selectAudioPlaybackDevice(TUICommonDefine.AudioPlaybackDevice.Speakerphone)
        } else {
            CallManager.instance.selectAudioPlaybackDevice(TUICommonDefine.AudioPlaybackDevice.Earpiece)
        }
    }

    private fun isCaller(userId: String): Boolean {
        val callerId = CallStore.shared.observerState.activeCall.value.inviterId
        return callerId == userId
    }

    companion object {
        private const val TAG = "CallMainActivity"
    }
}