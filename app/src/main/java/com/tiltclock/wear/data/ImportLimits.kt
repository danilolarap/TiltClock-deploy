package com.tiltclock.wear.data

object ImportLimits {
    const val MAX_BYTES=32L*1024*1024
    fun checkSize(size: Long) { require(size in 1..MAX_BYTES) { "GLB size exceeds 32 MiB or is empty" } }
    fun validId(id: String)=Regex("import_[a-f0-9]{32}").matches(id)
}
