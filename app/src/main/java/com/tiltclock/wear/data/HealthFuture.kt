package com.tiltclock.wear.data

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.*

internal suspend fun <T> ListenableFuture<T>.awaitHealth(): T = suspendCancellableCoroutine { continuation ->
    addListener({ try { continuation.resume(get()) } catch(failure: Exception) {
        continuation.resumeWithException(failure.cause ?: failure)
    } },Executor { it.run() })
    continuation.invokeOnCancellation { cancel(false) }
}
