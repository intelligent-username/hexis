package com.loc.hexis.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loc.hexis.core.data.notification.HexisNotificationManager.Companion.createNotificationChannel
import com.loc.hexis.core.interfaces.BiometricUtils
import com.loc.hexis.shared.ui.LocalWindowSizeClass
import com.loc.hexis.shared.ui.app.LaunchSource
import com.loc.hexis.shared.ui.components.InitialLoading
import com.loc.hexis.shared.ui.theme.HexisTheme
import com.loc.hexis.shared.ui.viewmodel.MainViewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        FileKit.init(this)

        createNotificationChannel(this)

        val launchSource =
            when {
                intent.hasExtra("shortcut_action") -> {
                    val action = intent.getStringExtra("shortcut_action")!!
                    if (action.startsWith("widget_")) LaunchSource.WIDGET else LaunchSource.SHORTCUT
                }
                intent.action == Intent.ACTION_MAIN &&
                    intent.hasCategory(Intent.CATEGORY_LAUNCHER) -> LaunchSource.LAUNCHER
                intent.getBooleanExtra("from_notification", false) -> LaunchSource.NOTIFICATION
                else -> LaunchSource.UNKNOWN
            }
        mainViewModel.setLaunchSource(launchSource)

        intent.getStringExtra("shortcut_action")?.let { mainViewModel.setShortcutAction(it) }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                val state by mainViewModel.state.collectAsStateWithLifecycle()

                var showContent by remember { mutableStateOf(false) }

                LaunchedEffect(state.isAppUnlocked, state.isBiometricLockOn) {
                    state.isBiometricLockOn?.let {
                        when {
                            !it && !showContent || state.isAppUnlocked -> showContent = true
                            else -> {
                                showBiometricPrompt(
                                    onSuccess = {
                                        mainViewModel.setAppUnlocked(true)
                                        showContent = true
                                    },
                                    onError = { errorCode, errString ->
                                        if (showContent) mainViewModel.setBiometricLock(!it)
                                        handleBiometricError(errorCode, errString) {
                                            showContent = true
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                HexisTheme(theme = state.theme) {
                    if (showContent) {
                        App(
                            state = state,
                            onDismissChangelog = { mainViewModel.dismissChangelog() },
                        )
                    } else {
                        InitialLoading(
                            dayOnHexis = state.dayOnHexis,
                            weeklyPoints = state.weeklyPoints,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val source =
            when {
                intent.hasExtra("shortcut_action") -> {
                    val action = intent.getStringExtra("shortcut_action")!!
                    if (action.startsWith("widget_")) LaunchSource.WIDGET else LaunchSource.SHORTCUT
                }
                intent.getBooleanExtra("from_notification", false) -> LaunchSource.NOTIFICATION
                else -> LaunchSource.UNKNOWN
            }
        mainViewModel.setLaunchSource(source)
        intent.getStringExtra("shortcut_action")?.let { mainViewModel.setShortcutAction(it) }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit, onError: (Int, CharSequence) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errorCode, errString)
                    }
                },
            )

        val biometricUtils by inject<BiometricUtils>()
        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Lock")
                .setAllowedAuthenticators(biometricUtils.getAuthenticators())
                .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun handleBiometricError(
        errorCode: Int,
        errString: CharSequence,
        onComplete: () -> Unit,
    ) {
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_CANCELED -> {
                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_SHORT).show()
                finish()
            }

            BiometricPrompt.ERROR_NO_BIOMETRICS,
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                mainViewModel.setAppUnlocked(true)
                mainViewModel.setBiometricLock(false)

                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_LONG).show()
                onComplete()
            }

            else -> {
                Toast.makeText(this, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
