package com.tencent.qcloud.tuikit.tuicallkit.manager.feature

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tencent.qcloud.tuicore.util.TUIBuild
import io.trtc.tuikit.atomicxcore.api.call.CallStore
import io.trtc.tuikit.atomicxcore.api.call.CallParticipantStatus
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CallingVibratorFeature(context: Context) {
    private val scope = MainScope()
    private val context: Context = context.applicationContext
    private val vibrator: Vibrator
    private var isVibrating = false

    init {
        if (TUIBuild.getVersionInt() >= Build.VERSION_CODES.S) {
            val vibratorManager = this.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            vibrator = this.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        registerObserver()
    }

    private fun registerObserver() {
        scope.launch {
            CallStore.shared.observerState.selfInfo.collect { selfInfo ->
                if (isCaller(selfInfo.id)) {
                    return@collect
                }
                when (selfInfo.status) {
                    CallParticipantStatus.Waiting -> startVibrating()
                    else -> stopVibrating()
                }
            }
        }
    }

    private fun startVibrating() {
        if (vibrator.hasVibrator()) {
            val pattern = longArrayOf(0, 500, 1500)
            isVibrating = true
            if (TUIBuild.getVersionInt() >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(pattern, 1)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(pattern, 1)
            }
        }
    }

    private fun stopVibrating() {
        if (isVibrating) {
            vibrator.cancel()
        }
        isVibrating = false
    }

    private fun isCaller(userId: String): Boolean {
        val callerId = CallStore.shared.observerState.activeCall.value.inviterId
        return callerId == userId
    }
}