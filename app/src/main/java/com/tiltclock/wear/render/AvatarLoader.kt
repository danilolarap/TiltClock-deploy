package com.tiltclock.wear.render

import com.google.android.filament.*
import com.google.android.filament.gltfio.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Called exclusively on the owning render thread. Keep the previous avatar until success. */
class AvatarLoader(private val engine: Engine, private val scene: Scene,
    private val changed: (RenderState) -> Unit, private val accepted: (String) -> Unit) {
    private val materials = UbershaderProvider(engine)
    private val loader = AssetLoader(engine,materials,EntityManager.get())
    private val resources = ResourceLoader(engine,true)
    private var pending: LoadedAvatar? = null
    private var source: ByteBuffer? = null
    private var started = 0L
    var current: LoadedAvatar? = null
        private set
    private val metrics = RenderMetrics()
    private var totalStart = 0L

    fun load(id: String, bytes: ByteArray, requestStart: Long) {
        cancelPending(); totalStart = requestStart
        var raw: FilamentAsset? = null
        try {
            changed(RenderState(loading=true,avatar=current?.id,animated=current?.animation?.available == true,
                playing=current?.animation?.playing != false))
            source = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.LITTLE_ENDIAN).apply { put(bytes); flip() }
            raw = loader.createAsset(source!!) ?: error("gltfio rejected GLB")
            require(raw.resourceUris.isEmpty()) { "Unexpected external resource" }
            // ResourceLoader initializes the native Animator, including assets without clips.
            check(resources.asyncBeginLoad(raw)) { "Resource loading could not start" }
            pending = LoadedAvatar(id,raw,engine); raw = null
            started = System.nanoTime()
        } catch (failure: Exception) {
            resources.asyncCancelLoad(); raw?.let(loader::destroyAsset); cancelPending(); error(failure)
        }
    }
    fun update(): Boolean {
        val candidate = pending ?: return false
        try {
            resources.asyncUpdateLoad()
            check(System.nanoTime()-started < 120_000_000_000L) { "Model load timed out" }
            if (resources.asyncGetLoadProgress() >= 1f) {
                // Drain readiness queue: completion alone does not guarantee renderables were uploaded.
                val ready = IntArray(candidate.asset.renderableEntities.size)
                val count = candidate.asset.popRenderables(ready)
                check(count == ready.size) { "Model resources incomplete ($count/${ready.size})" }
                candidate.asset.releaseSourceData(); source = null
                current?.destroy(loader,scene); current = candidate; pending = null
                scene.addEntities(candidate.asset.entities)
                resources.evictResourceData()
                metrics.loaded(candidate.id,(System.nanoTime()-totalStart)/1_000_000)
                changed(RenderState(avatar=candidate.id,animated=candidate.animation.available))
                accepted(candidate.id)
            }
        } catch (failure: Exception) { cancelPending(); error(failure) }
        return pending != null
    }
    fun error(failure: Throwable) = changed(RenderState(avatar=current?.id,
        animated=current?.animation?.available == true,playing=current?.animation?.playing != false,
        error=failure.message ?: failure.javaClass.simpleName))
    fun toggle() { current?.let { it.animation.playing = !it.animation.playing
        changed(RenderState(avatar=it.id,animated=it.animation.available,playing=it.animation.playing)) } }
    private fun cancelPending() {
        resources.asyncCancelLoad(); pending?.destroy(loader,scene); pending = null; source = null
        resources.evictResourceData()
    }
    fun destroy() {
        cancelPending(); current?.destroy(loader,scene); current = null
        resources.destroy(); loader.destroy(); materials.destroyMaterials(); materials.destroy()
    }
}
