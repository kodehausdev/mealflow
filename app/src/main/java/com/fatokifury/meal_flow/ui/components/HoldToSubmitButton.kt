package com.fatokifury.meal_flow.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun HoldToSubmitButton(
    onHeld: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdDurationMillis: Long = 1500L,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable (isHolding: Boolean, progress: Float) -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled) {
            isHolding = true
            val startTime = System.currentTimeMillis()
            // Loop while the button is still pressed
            while (isPressed && System.currentTimeMillis() - startTime < holdDurationMillis) {
                holdProgress = (System.currentTimeMillis() - startTime).toFloat() / holdDurationMillis
                delay(16) // ~60fps
            }

            // If the button is *still* pressed after the duration has passed, trigger the action
            if (System.currentTimeMillis() - startTime >= holdDurationMillis) {
                onHeld()
            }

            // Reset visuals after the hold is completed or released
            isHolding = false
            holdProgress = 0f
        } else {
            // Reset if the press is released early or if the button becomes disabled
            isHolding = false
            holdProgress = 0f
        }
    }

    Button(
        onClick = { /* Standard click is ignored in favor of the hold gesture */ },
        enabled = enabled,
        modifier = modifier,
        interactionSource = interactionSource
    ) {
        // The content lambda allows the caller to define what the button looks like
        // in its different states (normal, holding, loading).
        content(isHolding, holdProgress)
    }
}
