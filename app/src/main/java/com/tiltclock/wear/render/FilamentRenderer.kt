package com.tiltclock.wear.render

import android.os.Handler
import android.content.Context
import android.os.HandlerThread
import android.view.Surface
import com.google.android.filament.*
import com.google.android.filament.gltfio.Gltfio

/** One engine, one owning thread. All native loading/render/destruction happens off the UI thread. */
class FilamentRenderer(context: Context, private val transforms: TransformController,
    private val state: (RenderState) -> Unit, private val accepted: (String) -> Unit) {
    private val thread = HandlerThread("TiltClock3D").apply { start() }
    private val handler = Handler(thread.looper)
    private lateinit var engine: Engine
    private lateinit var renderer: Renderer
    private lateinit var view: View
    private lateinit var scene: Scene
    private lateinit var camera: Camera
    private lateinit var lights: SceneLighting
    private lateinit var shadow: ShadowPlane
    private lateinit var loader: AvatarLoader
    private var chain: SwapChain? = null
    private var surface: Surface? = null
    private var active = false
    private var ready = false
    @Volatile private var closed = false
    private var scale = 1f
    private var lastFrame = 0L
    private val metrics = RenderMetrics()
    init { handler.post {
        try {
            Gltfio.init(); engine = Engine.create(Engine.Backend.OPENGL)
            renderer = engine.createRenderer(); scene = engine.createScene(); view = engine.createView()
            camera = engine.createCamera(EntityManager.get().create())
            view.scene = scene; view.camera = camera
            renderer.clearOptions = Renderer.ClearOptions().apply { clear = true; clearColor = doubleArrayOf(0.0,0.0,0.0,0.0) }
            lights = SceneLighting(engine,scene,view,camera)
            shadow = ShadowPlane(engine,scene,context.assets.open("materials/shadow.filamat").use { it.readBytes() })
            loader = AvatarLoader(engine,scene,state,accepted); ready = true
        } catch (failure: Exception) {
            state(RenderState(error="3D: ${failure.message}")); destroyInternal()
        } catch (failure: LinkageError) {
            state(RenderState(error="3D: ${failure.message}")); destroyInternal()
        }
    } }
    fun attach(value: Surface, width: Int, height: Int) { handler.post {
        if (closed || !ready) { value.release(); return@post }
        detachInternal(); surface = value
        chain = engine.createSwapChain(value,SwapChain.CONFIG_TRANSPARENT)
        resizeInternal(width,height); schedule()
    } }
    fun resize(width: Int,height: Int) { handler.post { if (ready && !closed) resizeInternal(width,height) } }
    private fun resizeInternal(width: Int,height: Int) {
        view.viewport = Viewport(0,0,width.coerceAtLeast(1),height.coerceAtLeast(1))
        camera.setProjection(50.0,width.toDouble()/height.coerceAtLeast(1),.1,100.0,Camera.Fov.VERTICAL)
    }
    fun detach(after: () -> Unit = {}) {
        if (!handler.post { if (ready) detachInternal(); after() }) after()
    }
    private fun detachInternal() {
        handler.removeCallbacks(frame); chain?.let(engine::destroySwapChain); chain = null
        engine.flushAndWait(); surface?.release(); surface = null
    }
    fun setActive(value: Boolean) { handler.post {
        active = value; lastFrame = 0; metrics.reset(); handler.removeCallbacks(frame); schedule()
    } }
    fun setScale(value: Float) { handler.post { scale = value.coerceIn(.3f,3f) } }
    fun load(id: String,bytes: ByteArray,start: Long) { handler.post {
        if (!closed) {
            if (ready) loader.load(id,bytes,start) else state(RenderState(error="3D initialization unavailable"))
        }
    } }
    fun error(failure: Throwable) { handler.post { if (ready && !closed) loader.error(failure) } }
    fun toggleAnimation() { handler.post { if (ready && !closed) loader.toggle() } }
    private fun schedule() {
        // Surface attachment and lifecycle resume can arrive together: keep one frame loop.
        handler.removeCallbacks(frame)
        if (active && ready && !closed && chain != null) handler.post(frame)
    }
    private val frame = object : Runnable {
        override fun run() {
            if (closed || !active || chain == null || !ready) return
            val start = System.nanoTime()
            val dt = if (lastFrame == 0L) 0f else ((start-lastFrame)/1e9f).coerceIn(0f,.1f)
            lastFrame = start
            loader.update(); loader.current?.frame(transforms.frame(dt),scale,dt)
            val submitted = renderer.beginFrame(chain!!,start)
            if (submitted) { renderer.render(view); renderer.endFrame() }
            metrics.frame(start,submitted,System.nanoTime()-start)
            // 30 FPS target; no spin/wait, and no rendering while backgrounded/ambient.
            handler.postDelayed(this, (33L-(System.nanoTime()-start)/1_000_000).coerceAtLeast(1L))
        }
    }
    fun close() {
        if (closed) return
        closed = true
        handler.post {
            handler.removeCallbacks(frame)
            if (ready) destroyInternal()
            thread.quitSafely()
        }
    }
    private fun destroyInternal() {
        if (!::engine.isInitialized) return
        detachInternal()
        if (::loader.isInitialized) loader.destroy()
        if (::shadow.isInitialized) shadow.destroy()
        if (::lights.isInitialized) lights.destroy()
        if (::view.isInitialized) engine.destroyView(view)
        if (::scene.isInitialized) engine.destroyScene(scene)
        if (::renderer.isInitialized) engine.destroyRenderer(renderer)
        if (::camera.isInitialized) {
            engine.destroyCameraComponent(camera.entity); EntityManager.get().destroy(camera.entity)
        }
        engine.destroy(); ready = false
    }
}
