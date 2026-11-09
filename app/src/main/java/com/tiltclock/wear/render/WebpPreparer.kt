package com.tiltclock.wear.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.ByteArrayOutputStream

/** Lossless pixels -> PNG staging copy. Original Draco geometry and source assets stay intact. */
object WebpPreparer {
    fun prepare(bytes: ByteArray): ByteArray {
        val glb = GlbContainer.parse(bytes); val root = glb.json
        val images = root.optJSONArray("images") ?: return bytes
        if ((0 until images.length()).none { images.getJSONObject(it).optString("mimeType") == "image/webp" }) return bytes
        val views = root.getJSONArray("bufferViews")
        val output = ByteArrayOutputStream().apply { write(glb.binary) }
        var changed = false
        repeat(images.length()) { index ->
            val image = images.getJSONObject(index)
            if (image.optString("mimeType") == "image/webp") {
                val source = views.getJSONObject(image.getInt("bufferView"))
                val offset = source.optInt("byteOffset",0); val length = source.getInt("byteLength")
                require(offset >= 0 && length > 0 && offset.toLong()+length <= glb.binary.size)
                val bitmap = BitmapFactory.decodeByteArray(glb.binary,offset,length,
                    BitmapFactory.Options().apply { inScaled = false; inPreferredConfig = Bitmap.Config.ARGB_8888 })
                    ?: error("WebP decoding failed")
                val png = try { ByteArrayOutputStream().also {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))
                }.toByteArray() } finally { bitmap.recycle() }
                while (output.size()%4 != 0) output.write(0)
                val newIndex = views.length()
                views.put(JSONObject().put("buffer",0).put("byteOffset",output.size()).put("byteLength",png.size))
                output.write(png)
                image.put("bufferView",newIndex).put("mimeType","image/png"); changed = true
            }
        }
        if (!changed) return bytes
        val textures = root.optJSONArray("textures")
        if (textures != null) repeat(textures.length()) { index ->
            val texture = textures.getJSONObject(index); val extensions = texture.optJSONObject("extensions")
            extensions?.optJSONObject("EXT_texture_webp")?.let {
                texture.put("source",it.getInt("source")); extensions.remove("EXT_texture_webp")
                if (extensions.length() == 0) texture.remove("extensions")
            }
        }
        for (key in listOf("extensionsUsed","extensionsRequired")) root.optJSONArray(key)?.let { list ->
            for (i in list.length()-1 downTo 0) if (list.getString(i) == "EXT_texture_webp") list.remove(i)
        }
        root.getJSONArray("buffers").getJSONObject(0).put("byteLength",output.size())
        return GlbContainer(root,output.toByteArray()).encode()
    }
}
