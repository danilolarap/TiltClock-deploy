package com.tiltclock.wear

import com.tiltclock.wear.data.ImportLimits
import org.junit.Assert.*
import org.junit.Test

class ImportLimitsTest {
    @Test fun onlyGeneratedBasenamesCanResolvePrivateModels() {
        assertTrue(ImportLimits.validId("import_"+"a".repeat(32)))
        listOf("../avatar","import_../../secret","custom","import_"+"z".repeat(32)).forEach { assertFalse(ImportLimits.validId(it)) }
    }
    @Test fun sizeBoundsRejectEmptyAndOversizedDocuments() {
        ImportLimits.checkSize(1); ImportLimits.checkSize(ImportLimits.MAX_BYTES)
        for(size in listOf(0L,-1L,ImportLimits.MAX_BYTES+1)) {
            try { ImportLimits.checkSize(size); fail("Invalid size accepted") } catch(expected: IllegalArgumentException) { }
        }
    }
}
