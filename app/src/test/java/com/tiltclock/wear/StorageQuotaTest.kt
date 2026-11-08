package com.tiltclock.wear

import com.tiltclock.wear.data.ImportLimits
import org.junit.Assert.fail
import org.junit.Test

class StorageQuotaTest {
    @Test fun copyCannotCrossQuotaEvenWhenItStartedBelowLimit() {
        ImportLimits.checkStorage(ImportLimits.MAX_STORAGE-100,100)
        try { ImportLimits.checkStorage(ImportLimits.MAX_STORAGE-100,101); fail("Quota crossed") }
        catch(expected: IllegalArgumentException) { }
    }
    @Test fun overflowCannotBypassQuota() {
        try { ImportLimits.checkStorage(Long.MAX_VALUE,1); fail("Overflow accepted") }
        catch(expected: IllegalArgumentException) { }
    }
}
