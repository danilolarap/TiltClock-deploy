package com.tiltclock.wear.render

import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Strict GLB 2 reader; external resources are deliberately excluded in this phase. */
data class GlbContainer(val json: JSONObject, val binary: ByteArray) {
    fun encode(): ByteArray {
        // Android JSONObject escapes '/' as '\/'; cgltf's MIME lookup keeps that escape.
        // JSON permits a literal slash, which lets gltfio find its image/png provider.
        val text = json.toString().replace("\\/","/").toByteArray(Charsets.UTF_8)
        val jsonSize = (text.size+3) and -4; val binSize = (binary.size+3) and -4
        return ByteBuffer.allocate(12+8+jsonSize+8+binSize).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(0x46546c67); putInt(2); putInt(capacity())
            putInt(jsonSize); putInt(0x4e4f534a); put(text)
            repeat(jsonSize-text.size) { put(32.toByte()) }
            putInt(binSize); putInt(0x004e4942); put(binary)
        }.array()
    }
    companion object {
        fun parse(bytes: ByteArray): GlbContainer {
            require(bytes.size >= 28) { "Incomplete GLB" }
            val b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            require(b.int == 0x46546c67 && b.int == 2 && b.int == bytes.size) { "Invalid GLB header" }
            val n = b.int; require(n > 0 && n % 4 == 0 && n <= b.remaining()-12)
            require(b.int == 0x4e4f534a); val text = ByteArray(n); b.get(text)
            val length = b.int; require(length >= 0 && length % 4 == 0 && length == b.remaining()-4)
            require(b.int == 0x004e4942); val binary = ByteArray(length); b.get(binary)
            val json = JSONObject(String(text,Charsets.UTF_8))
            val buffers = json.getJSONArray("buffers")
            require(buffers.length() == 1 && !buffers.getJSONObject(0).has("uri")) { "Embedded GLB required" }
            val images = json.optJSONArray("images")
            if (images != null) repeat(images.length()) {
                require(!images.getJSONObject(it).has("uri")) { "External images not supported" }
            }
            return GlbContainer(json,binary)
        }
    }
}
