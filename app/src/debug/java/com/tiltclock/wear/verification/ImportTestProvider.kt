package com.tiltclock.wear.verification

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import com.tiltclock.wear.render.GlbContainer

/** Test documents only; not shipped in release or discoverable as a document picker. */
class ImportTestProvider: ContentProvider() {
    override fun onCreate()=true
    override fun getType(uri: Uri)="model/gltf-binary"
    override fun query(uri: Uri,p: Array<out String>?,s: String?,a: Array<out String>?,o: String?): Cursor?=null
    override fun insert(uri: Uri,values: ContentValues?): Uri?=null
    override fun update(uri: Uri,values: ContentValues?,s: String?,a: Array<out String>?)=0
    override fun delete(uri: Uri,s: String?,a: Array<out String>?)=0
    override fun openFile(uri: Uri,mode: String): ParcelFileDescriptor {
        require(mode=="r")
        val file=File(requireNotNull(context).cacheDir,"phase4-${uri.lastPathSegment}.glb")
        if(uri.lastPathSegment=="external") {
            val glb=GlbContainer.parse(context!!.assets.open("models/fox.glb").use { it.readBytes() })
            glb.json.getJSONArray("images").getJSONObject(0).put("uri","missing.png")
            file.writeBytes(glb.encode())
        }
        else if(uri.lastPathSegment=="valid") context!!.assets.open("models/fox.glb").use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        else file.writeBytes(byteArrayOf(1,2,3))
        return ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)
    }
}
