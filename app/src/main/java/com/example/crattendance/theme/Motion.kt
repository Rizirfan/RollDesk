package com.example.crattendance.theme

import androidx.compose.animation.core.spring

/**
 * Central motion system for RollDesk.
 *
 * Durations are in milliseconds. All animations respect the system animator
 * scale automatically when using Compose animation APIs.
 */
object Motion {
    const val Micro = 140
    const val Short = 180
    const val Nav = 300
    const val Tab = 240
    const val Long = 420
    const val Stagger = 40

    /** Bouncy spring for press-feedback scales. */
    val PressSpring = spring<Float>(dampingRatio = 0.82f, stiffness = 380f)

    /** Bouncy spring for interactive motion. */
    val InteractiveSpring = spring<Float>(dampingRatio = 0.9f, stiffness = 350f)
}
