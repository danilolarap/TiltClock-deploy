package com.tiltclock.wear.verification

import android.app.Instrumentation
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.os.Bundle
import com.google.android.filament.*
import com.google.android.filament.gltfio.Gltfio
import com.tiltclock.wear.data.*
import com.tiltclock.wear.render.*
import kotlinx.coroutines.runBlocking
import org.json.*
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Offscreen real JNI/GPU smoke test. Isolated from production; no fictional watch readings. */
class RendererInstrumentation : Instrumentation() {
    private var requested: String? = null
    override fun onCreate(arguments: Bundle?) {
        requested=arguments?.getString("model")
        super.onCreate(arguments); start()
    }
    override fun onStart() {
        val results=JSONArray(); val report=JSONObject().put("device",android.os.Build.MODEL)
            .put("sdk",android.os.Build.VERSION.SDK_INT).put("results",results)
        var success=false
        try {
            Gltfio.init(); val engine=Engine.create(Engine.Backend.OPENGL)
            val scene=engine.createScene(); val view=engine.createView(); val renderer=engine.createRenderer()
            val camera=engine.createCamera(EntityManager.get().create())
            view.scene=scene; view.camera=camera; view.viewport=Viewport(0,0,400,400)
            camera.setProjection(50.0,1.0,.1,100.0,Camera.Fov.VERTICAL)
            renderer.clearOptions=Renderer.ClearOptions().apply { clear=true; clearColor=doubleArrayOf(0.0,0.0,0.0,0.0) }
            val lights=SceneLighting(engine,scene,view,camera)
            val shadow=ShadowPlane(engine,scene,targetContext.assets.open("materials/shadow.filamat").use { it.readBytes() })
            val swap=engine.createSwapChain(400,400,SwapChain.CONFIG_READABLE or SwapChain.CONFIG_TRANSPARENT)
            var status=RenderState(); val loader=AvatarLoader(engine,scene,{status=it},{})
            val folder=File(targetContext.filesDir,"phase2").apply { mkdirs() }
            val repository=AvatarRepository(targetContext)
            try {
                for (id in AvatarCatalog.ids.filter { requested == null || requested == it }) {
                    Log.i("TiltClockProbe","Testing $id")
                    val start=System.nanoTime(); val bytes=runBlocking { repository.prepare(id) }
                    File(folder,"$id-prepared.glb").writeBytes(bytes)
                    val prepared=GlbContainer.parse(bytes)
                    val images=prepared.json.optJSONArray("images")
                    if (images != null) repeat(images.length()) { check(images.getJSONObject(it).optString("mimeType") != "image/webp") }
                    check(!String(bytes,Charsets.ISO_8859_1).contains("image\\/png")) { "Escaped MIME rejected by cgltf" }
                    if (id !in listOf("holo","fox")) {
                        val original=targetContext.assets.open("models/$id.glb").use { GlbContainer.parse(it.readBytes()) }
                        fun decode(container: GlbContainer,index: Int): Bitmap {
                            val image=container.json.getJSONArray("images").getJSONObject(index)
                            val slice=container.json.getJSONArray("bufferViews").getJSONObject(image.getInt("bufferView"))
                            return BitmapFactory.decodeByteArray(container.binary,slice.optInt("byteOffset",0),slice.getInt("byteLength"),
                                BitmapFactory.Options().apply { inScaled=false; inPreferredConfig=Bitmap.Config.ARGB_8888 })!!
                        }
                        repeat(images!!.length()) { index ->
                            val webp=decode(original,index); val png=decode(prepared,index)
                            check(webp.sameAs(png)) { "Texture pixels changed: $id/$index" }
                            webp.recycle(); png.recycle()
                        }
                    }
                    loader.load(id,bytes,start)
                    while (status.loading) { loader.update(); engine.flushAndWait(); Thread.sleep(10) }
                    check(status.error == null && loader.current?.id == id) { "Load $id failed: ${status.error}" }
                    check(status.animated == (id == "fox"))
                    val ms=(System.nanoTime()-start)/1_000_000
                    val frameStart=System.nanoTime(); var frames=0
                    repeat(30) {
                        loader.current!!.frame(Quaternion(),1f,1f/30)
                        if (renderer.beginFrame(swap)) { renderer.render(view); renderer.endFrame(); frames++ }
                        engine.flushAndWait()
                    }
                    val pixels=ByteBuffer.allocateDirect(400*400*4); val complete=CountDownLatch(1)
                    check(renderer.beginFrame(swap)); renderer.render(view)
                    renderer.readPixels(0,0,400,400,PixelBufferDescriptor(pixels,Texture.Format.RGBA,Texture.Type.UBYTE,
                        1,0,0,0,java.util.concurrent.Executor { it.run() },Runnable { complete.countDown() }))
                    renderer.endFrame(); engine.flushAndWait(); check(complete.await(15,TimeUnit.SECONDS))
                    pixels.rewind(); var visible=0; var colored=0
                    for (i in 0 until 400*400) if ((pixels.get(i*4+3).toInt() and 255)>0) {
                        visible++
                        val intensity=(0..2).sumOf { pixels.get(i*4+it).toInt() and 255 }
                        if (intensity > 60) colored++
                    }
                    check(visible > 100) { "Empty render for $id" }
                    check(colored > 100) { "Unlit or missing textures for $id" }
                    val bitmap=Bitmap.createBitmap(400,400,Bitmap.Config.ARGB_8888)
                    pixels.rewind(); bitmap.copyPixelsFromBuffer(pixels)
                    File(folder,"$id.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }; bitmap.recycle()
                    val memory=android.os.Debug.MemoryInfo(); android.os.Debug.getMemoryInfo(memory)
                    results.put(JSONObject().put("id",id).put("loaded",true).put("animated",status.animated)
                        .put("preparedBytes",bytes.size).put("loadMs",ms).put("processPssKb",memory.totalPss)
                        .put("frames",frames).put("offscreenElapsedMs",(System.nanoTime()-frameStart)/1_000_000)
                        .put("nonTransparentPixels",visible).put("coloredPixels",colored))
                    Log.i("TiltClockProbe",results.getJSONObject(results.length()-1).toString())
                    File(targetContext.filesDir,"phase2-report.json").writeText(report.toString(2))
                }
                val previous=loader.current
                loader.load("broken",byteArrayOf(1,2,3,4),System.nanoTime())
                check(loader.current === previous && status.error != null) { "Previous avatar lost after invalid load" }
                report.put("invalidLoadPreservesAvatar",true)
                loader.current!!.animation.playing=false
                loader.current!!.frame(Quaternion.axis(0f,1f,0f,.5f),1f,1f/30)
                report.put("rotationWhilePaused",true)
                success=true
            } finally {
                loader.destroy(); shadow.destroy(); lights.destroy(); engine.destroySwapChain(swap); engine.destroyView(view)
                engine.destroyRenderer(renderer); engine.destroyScene(scene); engine.destroyCameraComponent(camera.entity)
                EntityManager.get().destroy(camera.entity); engine.destroy()
            }
        } catch (failure: Throwable) { report.put("error",failure.stackTraceToString()) }
        report.put("success",success)
        File(targetContext.filesDir,"phase2-report.json").writeText(report.toString(2))
        finish(if (success) -1 else 0,Bundle().apply { putString("report",report.toString()) })
    }
}
