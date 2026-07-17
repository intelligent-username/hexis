package com.loc.hexis.shared.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay

fun fadeTransitionMetadata(durationMillis: Int = 500): Map<String, Any> = metadata {
    put(NavDisplay.TransitionKey) {
        fadeIn(animationSpec = tween(durationMillis)) togetherWith
            ExitTransition.KeepUntilTransitionsFinished
    }
    put(NavDisplay.PopTransitionKey) {
        EnterTransition.None togetherWith fadeOut(animationSpec = tween(durationMillis))
    }
    put(NavDisplay.PredictivePopTransitionKey) {
        EnterTransition.None togetherWith fadeOut(animationSpec = tween(durationMillis))
    }
}

fun verticalTransitionMetadata(durationMillis: Int = 500): Map<String, Any> = metadata {
    put(NavDisplay.TransitionKey) {
        slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis),
        ) togetherWith ExitTransition.KeepUntilTransitionsFinished
    }
    put(NavDisplay.PopTransitionKey) {
        EnterTransition.None togetherWith
            slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis))
    }
    put(NavDisplay.PredictivePopTransitionKey) {
        EnterTransition.None togetherWith
            slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis))
    }
}

fun horizontalTransitionMetadata(durationMillis: Int = 500): Map<String, Any> = metadata {
    put(NavDisplay.TransitionKey) {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis),
        ) togetherWith ExitTransition.KeepUntilTransitionsFinished
    }
    put(NavDisplay.PopTransitionKey) {
        EnterTransition.None togetherWith
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(durationMillis))
    }
    put(NavDisplay.PredictivePopTransitionKey) {
        EnterTransition.None togetherWith
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(durationMillis))
    }
}
