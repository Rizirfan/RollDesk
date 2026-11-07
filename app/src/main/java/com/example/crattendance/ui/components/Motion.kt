package com.example.crattendance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.crattendance.theme.Motion

/**
 * Scales content down while pressed, useful for cards and interactive tiles.
 * Reverts via a spring when released.
 */
@Composable
fun PressScale(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    pressedScale: Float = 0.97f,
    enabled: Boolean = true
) {
    var pressed by remember { mutableStateOf(false) }

    val state = rememberUpdatedState(onClick)

    Box(
        modifier = modifier
            .scale(
                scaleX = if (enabled && pressed) pressedScale else 1f,
                scaleY = if (enabled && pressed) pressedScale else 1f
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true },
                    onTap = { pressed = false; state.value() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Simple entry/exit animation for cards/sections that appear together.
 * fades and slides up slightly on enter; fades and slides down on exit.
 */
@Composable
fun CardEntrance(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Long = 0L,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = Motion.Short,
                delayMillis = delayMillis.toInt()
            )
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = Motion.Micro)
        )
    ) {
        content()
    }
}

/**
 * Animate the visibility of optional detail sections, e.g. expand/collapse rows.
 */
@Composable
fun DetailExpansion(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = tween(durationMillis = Motion.Short)
        ) + fadeIn(
            animationSpec = tween(durationMillis = Motion.Short)
        ),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = Motion.Micro)
        ) + fadeOut(
            animationSpec = tween(durationMillis = Motion.Micro)
        )
    ) {
        content()
    }
}

/**
 * Bottom-bar style slide-in / slide-out + fade for top-level chrome.
 */
@Composable
fun BottomChrome(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(durationMillis = Motion.Tab)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = Motion.Micro)
        )
    ) {
        content()
    }
}
