package com.tiltclock.wear.data

import android.content.Context
import com.tiltclock.wear.render.HoloGenerator
import com.tiltclock.wear.render.WebpPreparer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AvatarCatalog {
    val ids = listOf("holo","fox","pajaro","mapache","moneda","riot")
}

class AvatarRepository(private val context: Context) {
    suspend fun prepare(id: String): ByteArray = withContext(Dispatchers.IO) {
        require(id in AvatarCatalog.ids)
        if (id == "holo") HoloGenerator.generate()
        else context.assets.open("models/$id.glb").use { WebpPreparer.prepare(it.readBytes()) }
    }
}
