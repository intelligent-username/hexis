package com.loc.hexis.core.data

import android.content.Context
import android.content.Intent

object AppIntents {
    private const val MAIN_ACTIVITY = "com.loc.hexis.app.MainActivity"

    fun openMain(context: Context, shortcutAction: String, fromNotification: Boolean = false): Intent =
        Intent().setClassName(context, MAIN_ACTIVITY).apply {
            data = android.net.Uri.parse("hexis://action/$shortcutAction")
            putExtra("shortcut_action", shortcutAction)
            if (fromNotification) putExtra("from_notification", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
}
