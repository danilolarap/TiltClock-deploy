package com.tiltclock.wear.data

import android.content.*
import android.net.Uri

object DocumentAvailability {
    fun intent()=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE); type="*/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    fun supported(context: Context)=intent().resolveActivity(context.packageManager)!=null
}
