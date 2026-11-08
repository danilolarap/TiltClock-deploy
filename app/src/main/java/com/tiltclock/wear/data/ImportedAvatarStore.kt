package com.tiltclock.wear.data

import android.content.Context
import android.net.Uri
import com.tiltclock.wear.render.GlbContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.io.InputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Owns copies; no durable dependency on the provider's URI or temporary grants. */
class ImportedAvatarStore(private val context: Context) {
    private val folder=File(context.filesDir,"imports").apply { mkdirs() }
    fun file(id: String): File { require(ImportLimits.validId(id)); return File(folder,"$id.glb") }
    fun idForPath(path: String): String {
        val file=File(path)
        val id=file.nameWithoutExtension
        require(file.canonicalFile==this.file(id).canonicalFile)
        return id
    }
    suspend fun import(uri: Uri): String {
        require(uri.scheme=="content") { "Document URI required" }
        return copy { context.contentResolver.openInputStream(uri) }
    }
    val inbox: File get()=File(context.filesDir,"inbox/avatar.glb")
    suspend fun importInbox(): String {
        try { return copy { inbox.inputStream() } }
        finally { withContext(Dispatchers.IO) { inbox.delete() } }
    }
    private suspend fun copy(open: ()->InputStream?): String = withContext(Dispatchers.IO) {
        require(folder.listFiles().orEmpty().sumOf { it.length() } < 128L*1024*1024) { "Import storage quota exceeded" }
        val id="import_"+UUID.randomUUID().toString().replace("-","")
        val target=file(id); val temporary=File(folder,"$id.part")
        try {
            open().use { input ->
                requireNotNull(input) { "Document not readable" }
                temporary.outputStream().use { output ->
                    val chunk=ByteArray(64*1024); var total=0L
                    while(true) { currentCoroutineContext().ensureActive(); val read=input.read(chunk); if(read<0) break
                        total+=read; ImportLimits.checkSize(total); output.write(chunk,0,read) }
                    output.fd.sync()
                }
            }
            ImportLimits.checkSize(temporary.length())
            GlbContainer.parse(temporary.readBytes())
            check(temporary.renameTo(target)) { "Private model commit failed" }
            id
        } finally { temporary.delete() }
    }
    suspend fun read(id: String): ByteArray = withContext(Dispatchers.IO) {
        val value=file(id); ImportLimits.checkSize(value.length()); value.readBytes()
    }
    suspend fun retain(keep: Set<String>) = withContext(Dispatchers.IO) {
        folder.listFiles().orEmpty().filter { it.extension=="glb" && ImportLimits.validId(it.nameWithoutExtension) && it.nameWithoutExtension !in keep }.forEach { it.delete() }
    }
}
