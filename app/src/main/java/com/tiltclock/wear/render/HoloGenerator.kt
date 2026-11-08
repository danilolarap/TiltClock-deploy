package com.tiltclock.wear.render

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

/** Actual torus wire edges and subdivided icosahedron; no image, animation or automatic spin. */
object HoloGenerator {
    fun generate(): ByteArray {
        val bin = ByteArrayOutputStream(); val views = JSONArray(); val accessors = JSONArray()
        fun attribute(points: List<FloatArray>, bounds: Boolean): Int {
            val bytes = ByteBuffer.allocate(points.size*12).order(ByteOrder.LITTLE_ENDIAN)
            points.forEach { p -> p.forEach { bytes.putFloat(it) } }
            val view = views.length()
            views.put(JSONObject().put("buffer",0).put("byteOffset",bin.size()).put("byteLength",bytes.capacity()))
            bin.write(bytes.array())
            val accessor = JSONObject().put("bufferView",view).put("componentType",5126)
                .put("count",points.size).put("type","VEC3")
            if (bounds) {
                accessor.put("min",JSONArray((0..2).map { axis -> points.minOf { it[axis] } }))
                accessor.put("max",JSONArray((0..2).map { axis -> points.maxOf { it[axis] } }))
            }
            return accessors.length().also { accessors.put(accessor) }
        }
        fun primitive(p: List<FloatArray>, normals: List<FloatArray>, mode: Int, material: Int) =
            JSONObject().put("attributes",JSONObject().put("POSITION",attribute(p,true)).put("NORMAL",attribute(normals,false)))
                .put("mode",mode).put("material",material)
        val ringPoints = mutableListOf<FloatArray>(); val ringNormals = mutableListOf<FloatArray>()
        for ((radius,rotation) in listOf(.65f to Quaternion.axis(1f,0f,0f,PI.toFloat()/3),
            .5f to Quaternion.axis(0f,1f,0f,PI.toFloat()/4))) {
            val matrix = rotation.matrix()
            fun rotate(p: FloatArray) = FloatArray(3) { r -> matrix[r]*p[0]+matrix[4+r]*p[1]+matrix[8+r]*p[2] }
            fun vertex(i: Int,j: Int): Pair<FloatArray,FloatArray> {
                val u = i*2*PI/100; val v = j*2*PI/16
                val normal = floatArrayOf((cos(v)*cos(u)).toFloat(),(cos(v)*sin(u)).toFloat(),sin(v).toFloat())
                val p = floatArrayOf(((radius+.012*cos(v))*cos(u)).toFloat(),
                    ((radius+.012*cos(v))*sin(u)).toFloat(),(.012*sin(v)).toFloat())
                return rotate(p) to rotate(normal)
            }
            for (i in 0 until 100) for (j in 0 until 16) {
                // Two triangles per quad: three unique wire edges per vertex.
                for ((a,b) in listOf((i to j) to (i+1 to j),(i to j) to (i to j+1),(i to j) to (i+1 to j+1))) {
                    for ((u,v) in listOf(a,b)) { val (p,n) = vertex(u,v); ringPoints.add(p); ringNormals.add(n) }
                }
            }
        }
        val t = ((1+sqrt(5.0))/2).toFloat()
        val vertices = listOf(floatArrayOf(-1f,t,0f),floatArrayOf(1f,t,0f),floatArrayOf(-1f,-t,0f),floatArrayOf(1f,-t,0f),
            floatArrayOf(0f,-1f,t),floatArrayOf(0f,1f,t),floatArrayOf(0f,-1f,-t),floatArrayOf(0f,1f,-t),
            floatArrayOf(t,0f,-1f),floatArrayOf(t,0f,1f),floatArrayOf(-t,0f,-1f),floatArrayOf(-t,0f,1f))
        val faces = listOf(0,11,5,0,5,1,0,1,7,0,7,10,0,10,11,1,5,9,5,11,4,11,10,2,10,7,6,7,1,8,
            3,9,4,3,4,2,3,2,6,3,6,8,3,8,9,4,9,5,2,4,11,6,2,10,8,6,7,9,8,1).chunked(3)
        val core = mutableListOf<FloatArray>(); val normals = mutableListOf<FloatArray>()
        for (face in faces) {
            val a=vertices[face[0]]; val b=vertices[face[1]]; val c=vertices[face[2]]
            fun point(i: Int,j: Int): FloatArray = FloatArray(3) { k -> (a[k]*(3-i-j)+b[k]*i+c[k]*j)/3 }
            fun triangle(vararg points: FloatArray) { for (p in points) {
                val n = sqrt(p.sumOf { (it*it).toDouble() }).toFloat()
                val normal = FloatArray(3) { p[it]/n }; normals.add(normal); core.add(FloatArray(3) { normal[it]*.28f })
            } }
            for (i in 0 until 3) for (j in 0 until 3-i) {
                triangle(point(i,j),point(i+1,j),point(i,j+1))
                if (i+j < 2) triangle(point(i+1,j),point(i+1,j+1),point(i,j+1))
            }
        }
        fun material(color: List<Double>, metal: Double, rough: Double) = JSONObject().put("doubleSided",true)
            .put("pbrMetallicRoughness",JSONObject().put("baseColorFactor",JSONArray(color))
                .put("metallicFactor",metal).put("roughnessFactor",rough))
        fun linear(c: Int): Double { val s=c/255.0; return if (s<=.04045) s/12.92 else ((s+.055)/1.055).pow(2.4) }
        val primitives = JSONArray().put(primitive(ringPoints,ringNormals,1,0)).put(primitive(core,normals,4,1))
        val root = JSONObject().put("asset",JSONObject().put("version","2.0").put("generator","TiltClock HoloGenerator"))
            .put("scene",0).put("scenes",JSONArray().put(JSONObject().put("nodes",JSONArray().put(0))))
            .put("nodes",JSONArray().put(JSONObject().put("mesh",0)))
            .put("meshes",JSONArray().put(JSONObject().put("primitives",primitives)))
            .put("materials",JSONArray().put(material(listOf(linear(6),linear(182),linear(212),1.0),.9,.1))
                .put(material(listOf(linear(8),linear(145),linear(178),1.0),.85,.15)))
            .put("bufferViews",views).put("accessors",accessors)
            .put("buffers",JSONArray().put(JSONObject().put("byteLength",bin.size())))
        return GlbContainer(root,bin.toByteArray()).encode()
    }
}
