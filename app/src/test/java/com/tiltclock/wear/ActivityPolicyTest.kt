package com.tiltclock.wear

import com.tiltclock.wear.ui.ActivityPolicy
import org.junit.Assert.*
import org.junit.Test

class ActivityPolicyTest {
    @Test fun backgroundAndNonInteractiveNeverRenderOrCollect() {
        for(resumed in listOf(false,true)) for(interactive in listOf(false,true)) {
            if(!resumed || !interactive) {
                assertFalse(ActivityPolicy.measurements(resumed,interactive))
                assertFalse(ActivityPolicy.render(resumed,interactive,false))
            }
        }
    }
    @Test fun settingsBlockManipulationButNotForegroundMeasurements() {
        assertTrue(ActivityPolicy.measurements(true,true))
        assertFalse(ActivityPolicy.render(true,true,true))
        assertTrue(ActivityPolicy.render(true,true,false))
    }
}
