package com.tiltclock.wear.render

data class RenderState(
    val loading: Boolean = false,
    val avatar: String? = null,
    val animated: Boolean = false,
    val playing: Boolean = true,
    val error: String? = null,
)
