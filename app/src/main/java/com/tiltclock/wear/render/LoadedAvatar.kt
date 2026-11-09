package com.tiltclock.wear.render

import com.google.android.filament.*
import com.google.android.filament.gltfio.*

/** Asset-owned entities are separate from our parent transforms and from the animator skeleton. */
class LoadedAvatar(val id: String, val asset: FilamentAsset, private val engine: Engine) {
    private val orientation = EntityManager.get().create()
    private val centering = EntityManager.get().create()
    private val scale = if (id == "holo") 1f else ModelTransform.baseScale(asset.boundingBox.halfExtent)
    val animation = AnimationController(asset.instance.animator)
    private var previousRotation: Quaternion? = null
    private var previousScale = Float.NaN
    init {
        val tm = engine.transformManager
        tm.create(orientation); tm.create(centering)
        tm.setParent(tm.getInstance(centering),tm.getInstance(orientation))
        if (id != "holo") tm.setTransform(tm.getInstance(centering),ModelTransform.center(asset.boundingBox.center))
        tm.setParent(tm.getInstance(asset.root),tm.getInstance(centering))
        for (entity in asset.renderableEntities) {
            val instance = engine.renderableManager.getInstance(entity)
            engine.renderableManager.setCastShadows(instance,true)
            engine.renderableManager.setReceiveShadows(instance,true)
        }
    }
    fun frame(rotation: Quaternion, userScale: Float, dt: Float) {
        if (rotation != previousRotation || userScale != previousScale) {
            engine.transformManager.setTransform(engine.transformManager.getInstance(orientation),rotation.matrix(scale*userScale))
            previousRotation = rotation; previousScale = userScale
        }
        animation.frame(dt)
    }
    fun destroy(loader: AssetLoader, scene: Scene) {
        scene.removeEntities(asset.entities); loader.destroyAsset(asset)
        engine.destroyEntity(centering); engine.destroyEntity(orientation)
        EntityManager.get().destroy(centering); EntityManager.get().destroy(orientation)
    }
}
