package com.shub39.grit.core.interfaces

interface BiometricUtils {
    fun getAuthenticators(): Int

    fun authenticationAvailable(): Boolean
}