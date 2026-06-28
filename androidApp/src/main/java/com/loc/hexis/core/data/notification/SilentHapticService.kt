package com.loc.hexis.core.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class SilentHapticService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "silent_haptic_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, "System Sync", NotificationManager.IMPORTANCE_MIN).apply {
                lockscreenVisibility = Notification.VISIBILITY_SECRET
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(1001, notification)
        triggerSilentVibration()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun triggerSilentVibration() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator

        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Hexis::HapticWakeLock")
        wakeLock.acquire(1000)

        try {
            if (vibrator.hasVibrator()) {
                val timings = longArrayOf(0, 200, 100, 200)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            }
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SilentHapticService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
