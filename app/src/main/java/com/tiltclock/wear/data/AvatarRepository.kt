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
    val imports=ImportedAvatarStore(context)
    suspend fun prepare(id: String): ByteArray = withContext(Dispatchers.IO) {
        if(ImportLimits.validId(id)) return@withContext WebpPreparer.prepare(imports.read(id))
        require(id in AvatarCatalog.ids)
        if (id == "holo") HoloGenerator.generate()
        // Bundled models are prepared offline: avoid parsing and copying their buffers on the watch.
        else context.assets.open("models/$id.glb").use { it.readBytes() }
    }
}
