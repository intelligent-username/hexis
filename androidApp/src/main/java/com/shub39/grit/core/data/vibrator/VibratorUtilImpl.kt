package com.shub39.grit.core.data.vibrator

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.shub39.grit.core.interfaces.VibratorUtil
import org.koin.core.annotation.Single

@Single(binds = [VibratorUtil::class])
class VibratorUtilImpl(
    private val context: Context,
) : VibratorUtil {

    override fun buzz() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager ?: return
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val burst = VibrationEffect.createOneShot(500L, 255)
            vibrator.vibrate(burst)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500L)
        }
    }
}
