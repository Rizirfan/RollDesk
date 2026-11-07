package com.example.crattendance.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import com.example.crattendance.theme.Motion

/**
 * Forward navigation transition.
 *
 * Tab-to-tab: fade (the persistent shell stays in place).
 * Push to detail: fade.
 */
internal fun appEnterTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = fadeIn(
            animationSpec = tween(
                durationMillis = Motion.Tab
            )
        ),
        initialContentExit = fadeOut(
            animationSpec = tween(
                durationMillis = Motion.Tab
            )
        )
    )
}

/**
 * Pop navigation transition.
 */
internal fun appPopTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = fadeIn(
            animationSpec = tween(
                durationMillis = Motion.Nav
            )
        ),
        initialContentExit = fadeOut(
            animationSpec = tween(
                durationMillis = Motion.Nav
            )
        )
    )
}

/**
 * Predictive pop transition used for the system back gesture.
 */
internal fun appPredictivePopTransition(swipeEdge: Int): ContentTransform {
    return ContentTransform(
        targetContentEnter = fadeIn(
            animationSpec = tween(
                durationMillis = Motion.Nav
            )
        ),
        initialContentExit = fadeOut(
            animationSpec = tween(
                durationMillis = Motion.Nav
            )
        )
    )
}
