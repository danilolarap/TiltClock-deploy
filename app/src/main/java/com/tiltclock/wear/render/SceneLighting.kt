package com.tiltclock.wear.render

import com.google.android.filament.*
import kotlin.math.ln

/** Physical light units are starting calibration values, not Three.js intensity equivalents. */
class SceneLighting(private val engine: Engine, scene: Scene, view: View, camera: Camera) {
    private val directional = EntityManager.get().create()
    private val point = EntityManager.get().create()
    private val ambient: IndirectLight
    private val tone = ToneMapper.ACES()
    private val grading: ColorGrading
    init {
        camera.setExposure(16f,1f/125f,100f)
        camera.lookAt(0.0,0.0,3.8,0.0,0.0,0.0,0.0,1.0,0.0)
        val shadow = LightManager.ShadowOptions().apply { mapSize = 1024; shadowFar = 10f; shadowNearHint = .1f }
        LightManager.Builder(LightManager.Type.DIRECTIONAL).color(1f,1f,1f).intensity(100_000f)
            .direction(-2f,-4f,-3f).castShadows(true).shadowOptions(shadow).build(engine,directional)
        LightManager.Builder(LightManager.Type.POINT).color(.002f,.468f,.658f).intensity(3_000_000f)
            .position(-2f,0f,2f).falloff(6f).castShadows(false).build(engine,point)
        // First two SH bands approximate the white/blue-gray hemisphere; no opaque skybox.
        ambient = IndirectLight.Builder().irradiance(2,floatArrayOf(
            .75f,.79f,.86f, .15f,.14f,.11f, 0f,0f,0f, 0f,0f,0f)).intensity(30_000f).build(engine)
        scene.indirectLight = ambient; scene.addEntities(intArrayOf(directional,point))
        grading = ColorGrading.Builder().toneMapper(tone).exposure((ln(1.2)/ln(2.0)).toFloat()).build(engine)
        view.colorGrading = grading
        view.blendMode = View.BlendMode.TRANSLUCENT
        view.antiAliasing = View.AntiAliasing.FXAA
        view.dithering = View.Dithering.NONE
    }
    fun destroy() {
        engine.destroyEntity(directional); engine.destroyEntity(point)
        EntityManager.get().destroy(directional); EntityManager.get().destroy(point)
        engine.destroyIndirectLight(ambient); engine.destroyColorGrading(grading)
    }
}
