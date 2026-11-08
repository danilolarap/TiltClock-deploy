package com.tiltclock.wear.render

import android.os.Debug
import android.util.Log
import org.json.JSONObject

/** Diagnostic log output, never a source of product measurements. GPU memory is not inferred. */
class RenderMetrics {
    private var start = 0L
    private var frames = 0
    private var skipped = 0
    private var cpuNanos = 0L
    fun loaded(id: String, loadMs: Long) {
        val memory = Debug.MemoryInfo(); Debug.getMemoryInfo(memory)
        Log.i("TiltClockMetrics",JSONObject().put("event","loaded").put("avatar",id)
            .put("loadMs",loadMs).put("processPssKb",memory.totalPss)
            .put("nativeHeapBytes",Debug.getNativeHeapAllocatedSize()).put("gpuMemoryBytes",JSONObject.NULL).toString())
        start = 0L; frames = 0; skipped = 0; cpuNanos = 0
    }
    fun frame(now: Long, submitted: Boolean, cpu: Long) {
        if (start == 0L) start = now
        if (submitted) frames++ else skipped++
        cpuNanos += cpu
        if (now-start >= 5_000_000_000L) {
            Log.i("TiltClockMetrics",JSONObject().put("event","frames").put("submittedFps",frames*1e9/(now-start))
                .put("skipped",skipped).put("meanCpuSubmitMs",cpuNanos/1e6/(frames+skipped)).toString())
            start = now; frames = 0; skipped = 0; cpuNanos = 0
        }
    }
    fun reset() { start = 0; frames = 0; skipped = 0; cpuNanos = 0 }
}
