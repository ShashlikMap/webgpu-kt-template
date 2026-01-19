package com.shashlikmap.webgpukt.webgpu.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

fun FloatArray.toByteBuffer(): ByteBuffer {
    val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(size * Float.SIZE_BYTES)
    byteBuffer.order(ByteOrder.nativeOrder())
    val floatBuffer: FloatBuffer = byteBuffer.asFloatBuffer()
    floatBuffer.put(this)
    byteBuffer.rewind()
    return byteBuffer
}

fun IntArray.toByteBuffer(): ByteBuffer {
    val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(size * Int.SIZE_BYTES)
    byteBuffer.order(ByteOrder.nativeOrder())
    val intBuffer: IntBuffer = byteBuffer.asIntBuffer()
    intBuffer.put(this)
    byteBuffer.rewind()
    return byteBuffer
}

private const val ALIGNMENT = 4L

val FloatArray.paddedSize: Long
    get() = padded((size * Float.SIZE_BYTES).toLong())

val ByteBuffer.paddedSize: Long
    get() = padded((remaining()).toLong())

private fun padded(dataSize: Long) = (dataSize + 3) and -ALIGNMENT