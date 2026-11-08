package com.tiltclock.wear.ui

/** Ambient/background/settings must never run the interactive 3D loop. */
object ActivityPolicy {
    fun measurements(resumed: Boolean,interactive: Boolean)=resumed && interactive
    fun render(resumed: Boolean,interactive: Boolean,blocked: Boolean)=measurements(resumed,interactive) && !blocked
}
