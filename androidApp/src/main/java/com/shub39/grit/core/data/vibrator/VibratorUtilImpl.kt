package com.shub39.grit.core.data.vibrator

import android.content.Context
import com.shub39.grit.core.data.notification.SilentHapticService
import com.shub39.grit.core.interfaces.VibratorUtil
import org.koin.core.annotation.Single

@Single(binds = [VibratorUtil::class])
class VibratorUtilImpl(
    private val context: Context,
) : VibratorUtil {

    override fun buzz() {
        SilentHapticService.start(context)
    }
}
