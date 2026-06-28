package com.loc.hexis.shared.ui

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual fun blurPossible(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S