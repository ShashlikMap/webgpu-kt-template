package com.shashlikmap.webgpukt.mesh

import android.content.Context
import androidx.annotation.RawRes
import com.shashlikmap.webgpukt.webgpu.Vertex
import com.shashlikmap.webgpukt.webgpu.utils.Vec3
import com.shashlikmap.webgpukt.webgpu.utils.Vec4
import de.javagl.obj.Obj
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import de.javagl.obj.ReadableObj

data class Mesh(val vertices: List<Vertex>, val indices: List<Int>)

object MeshLoader {

    fun loadMesh(context: Context, @RawRes obj: Int): Mesh {
        val obj: Obj? = ObjUtils.convertToRenderable(
            ObjReader.read(context.resources.openRawResource(obj).bufferedReader())
        )


        val vertices = (0 until obj!!.numVertices).map { index ->
            val vertex = obj.getVertex(index)
            val normal = obj.getNormal(index)
            Vertex(
                position = Vec3(vertex.x, vertex.y, vertex.z),
                normal = Vec3(normal.x, normal.y, normal.z),
                color = Vec4(1.0f, 1.0f, 1.0f, 1.0f)
            )
        }

        val indices = obj.getFaceVertexIndices()
        return Mesh(vertices, indices)
    }

    private fun ReadableObj.getFaceVertexIndices(): List<Int> {
        val target = ArrayList<Int>()
        for (i in 0..<numFaces) {
            val face = getFace(i)
            for (j in 0..<face.numVertices) {
                target.add(face.getVertexIndex(j))
            }
        }
        return target
    }
}