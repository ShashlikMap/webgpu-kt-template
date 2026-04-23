package com.shashlikmap.webgpukt.webgpu.utils

import androidx.webgpu.GPUVertexAttribute
import androidx.webgpu.VertexFormat
import androidx.webgpu.VertexFormat.Companion.Float16
import androidx.webgpu.VertexFormat.Companion.Float16x2
import androidx.webgpu.VertexFormat.Companion.Float16x4
import androidx.webgpu.VertexFormat.Companion.Float32
import androidx.webgpu.VertexFormat.Companion.Float32x2
import androidx.webgpu.VertexFormat.Companion.Float32x3
import androidx.webgpu.VertexFormat.Companion.Float32x4
import androidx.webgpu.VertexFormat.Companion.Sint16
import androidx.webgpu.VertexFormat.Companion.Sint16x2
import androidx.webgpu.VertexFormat.Companion.Sint16x4
import androidx.webgpu.VertexFormat.Companion.Sint32
import androidx.webgpu.VertexFormat.Companion.Sint32x2
import androidx.webgpu.VertexFormat.Companion.Sint32x3
import androidx.webgpu.VertexFormat.Companion.Sint32x4
import androidx.webgpu.VertexFormat.Companion.Sint8
import androidx.webgpu.VertexFormat.Companion.Sint8x2
import androidx.webgpu.VertexFormat.Companion.Sint8x4
import androidx.webgpu.VertexFormat.Companion.Snorm16
import androidx.webgpu.VertexFormat.Companion.Snorm16x2
import androidx.webgpu.VertexFormat.Companion.Snorm16x4
import androidx.webgpu.VertexFormat.Companion.Snorm8
import androidx.webgpu.VertexFormat.Companion.Snorm8x2
import androidx.webgpu.VertexFormat.Companion.Snorm8x4
import androidx.webgpu.VertexFormat.Companion.Uint16
import androidx.webgpu.VertexFormat.Companion.Uint16x2
import androidx.webgpu.VertexFormat.Companion.Uint16x4
import androidx.webgpu.VertexFormat.Companion.Uint32
import androidx.webgpu.VertexFormat.Companion.Uint32x2
import androidx.webgpu.VertexFormat.Companion.Uint32x3
import androidx.webgpu.VertexFormat.Companion.Uint32x4
import androidx.webgpu.VertexFormat.Companion.Uint8
import androidx.webgpu.VertexFormat.Companion.Uint8x2
import androidx.webgpu.VertexFormat.Companion.Uint8x4
import androidx.webgpu.VertexFormat.Companion.Unorm10_10_10_2
import androidx.webgpu.VertexFormat.Companion.Unorm16
import androidx.webgpu.VertexFormat.Companion.Unorm16x2
import androidx.webgpu.VertexFormat.Companion.Unorm16x4
import androidx.webgpu.VertexFormat.Companion.Unorm8
import androidx.webgpu.VertexFormat.Companion.Unorm8x2
import androidx.webgpu.VertexFormat.Companion.Unorm8x4
import androidx.webgpu.VertexFormat.Companion.Unorm8x4BGRA

/**
 * Return the byte size of [VertexFormat]
 */
val @VertexFormat.Type Int.byteSize: Long
    get() {
        return when (this) {
            Uint8, Sint8, Unorm8, Snorm8 -> 1L
            Uint8x2, Sint8x2, Unorm8x2, Snorm8x2, Uint16, Sint16, Unorm16, Snorm16, Float16 -> 2L
            Uint8x4, Sint8x4, Unorm8x4, Snorm8x4, Uint16x2, Sint16x2, Unorm16x2, Snorm16x2, Float16x2, Float32, Uint32, Sint32, Unorm10_10_10_2, Unorm8x4BGRA -> 4L
            Uint16x4, Sint16x4, Unorm16x4, Snorm16x4, Float16x4, Float32x2, Uint32x2, Sint32x2 -> 8L
            Float32x3, Uint32x3, Sint32x3 -> 12L
            Float32x4, Uint32x4, Sint32x4 -> 16L
            else -> throw Exception("Unknown VertexFormat: $this")
        }
    }

val Array<@VertexFormat.Type Int>.byteSize: Long
    get() = sumOf { it.byteSize }

/**
 * Generates [GPUVertexAttribute] array for the list of [VertexFormat]
 */
val Array<@VertexFormat.Type Int>.gpuVertexAttributes: Array<GPUVertexAttribute>
    get() = foldIndexed(arrayListOf<GPUVertexAttribute>()) { index, acc, type ->
        val offset = acc.lastOrNull()?.let { attr ->
            attr.offset + attr.format.byteSize
        } ?: 0L
        acc += GPUVertexAttribute(
            format = type,
            offset = offset,
            shaderLocation = index
        )
        acc
    }.toTypedArray()
