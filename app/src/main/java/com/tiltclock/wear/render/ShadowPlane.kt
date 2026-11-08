package com.tiltclock.wear.render

import com.google.android.filament.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Invisible 5x5 horizontal receiver, Y=-1.1; shadow opacity .25 in compiled material. */
class ShadowPlane(private val engine: Engine, private val scene: Scene, materialBytes: ByteArray) {
    private val entity = EntityManager.get().create()
    private val material = Material.Builder().payload(ByteBuffer.wrap(materialBytes),materialBytes.size).build(engine)
    private val instance = material.createInstance()
    private val vertices = VertexBuffer.Builder().vertexCount(4).bufferCount(2)
        .attribute(VertexBuffer.VertexAttribute.POSITION,0,VertexBuffer.AttributeType.FLOAT3,0,12)
        .attribute(VertexBuffer.VertexAttribute.TANGENTS,1,VertexBuffer.AttributeType.FLOAT4,0,16).build(engine)
    private val indices = IndexBuffer.Builder().indexCount(6).bufferType(IndexBuffer.Builder.IndexType.USHORT).build(engine)
    init {
        val p = ByteBuffer.allocateDirect(48).order(ByteOrder.nativeOrder()).asFloatBuffer()
        p.put(floatArrayOf(-2.5f,-1.1f,-2.5f, -2.5f,-1.1f,2.5f, 2.5f,-1.1f,2.5f, 2.5f,-1.1f,-2.5f)).flip()
        val i = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder()).asShortBuffer()
        i.put(shortArrayOf(0,1,2,0,2,3)).flip()
        vertices.setBufferAt(engine,0,p); indices.setBuffer(engine,i)
        val tangent = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer()
        repeat(4) { tangent.put(floatArrayOf(-.70710677f,0f,0f,.70710677f)) }; tangent.flip()
        vertices.setBufferAt(engine,1,tangent)
        RenderableManager.Builder(1).boundingBox(Box(0f,-1.1f,0f,2.5f,.001f,2.5f))
            .geometry(0,RenderableManager.PrimitiveType.TRIANGLES,vertices,indices).material(0,instance)
            .castShadows(false).receiveShadows(true).build(engine,entity)
        scene.addEntity(entity)
    }
    fun destroy() {
        scene.remove(entity); engine.destroyEntity(entity); EntityManager.get().destroy(entity)
        engine.destroyVertexBuffer(vertices); engine.destroyIndexBuffer(indices)
        engine.destroyMaterialInstance(instance); engine.destroyMaterial(material)
    }
}
