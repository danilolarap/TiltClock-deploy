package com.tiltclock.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import com.tiltclock.wear.render.ZoomController
import kotlinx.coroutines.*

@Composable
fun ZoomButton(direction: Int, action: (Int,Boolean) -> Unit) {
    val palette = LocalWatchPalette.current
    Box(Modifier.size(48.dp).semantics {
        contentDescription = if (direction > 0) "+" else "−"
        onClick { action(direction,false); true }
    }.pointerInput(direction) {
        val scope = CoroutineScope(currentCoroutineContext())
        awaitEachGesture {
            val down = awaitFirstDown(); down.consume()
            action(direction,false)
            // pointer input restricted scope: a separate coroutine performs held repetitions.
            val job = scope.launch {
                delay(ZoomController.HOLD_DELAY_MS)
                while (isActive) { action(direction,true); delay(ZoomController.REPEAT_MS) }
            }
            try {
                val up = waitForUpOrCancellation()
                up?.consume()
            } finally { job.cancel() }
        }
    },contentAlignment=Alignment.Center) {
        Box(Modifier.size(24.dp).background(palette.glass,CircleShape),contentAlignment=Alignment.Center) {
            BasicText(if (direction > 0) "+" else "−",style=TextStyle(color=palette.text,fontSize=18.sp))
        }
    }
}
