package com.loc.hexis.core.interfaces

interface BiometricUtils {
    fun getAuthenticators(): Int

    fun authenticationAvailable(): Boolean
}
