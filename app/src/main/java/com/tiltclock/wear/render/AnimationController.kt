package com.tiltclock.wear.render

import com.google.android.filament.gltfio.Animator

class AnimationController(private val animator: Animator) {
    val available = animator.animationCount > 0
    var playing = true
    private var elapsed = 0f
    fun frame(seconds: Float) {
        if (!available) return
        if (playing) elapsed += seconds
        val duration = animator.getAnimationDuration(0)
        animator.applyAnimation(0, if (duration > 0) elapsed % duration else 0f)
        animator.updateBoneMatrices()
    }
}
