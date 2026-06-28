package com.loc.hexis.core.data.vibrator

import android.content.Context
import com.loc.hexis.core.data.notification.SilentHapticService
import com.loc.hexis.core.interfaces.VibratorUtil
import org.koin.core.annotation.Single

@Single(binds = [VibratorUtil::class])
class VibratorUtilImpl(
    private val context: Context,
) : VibratorUtil {

    override fun buzz() {
        SilentHapticService.start(context)
    }
}
