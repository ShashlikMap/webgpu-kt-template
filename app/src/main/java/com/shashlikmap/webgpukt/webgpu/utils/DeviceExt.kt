package com.shashlikmap.webgpukt.webgpu.utils

import androidx.webgpu.BufferUsage
import androidx.webgpu.GPUBuffer
import androidx.webgpu.GPUBufferDescriptor
import androidx.webgpu.GPUDevice
import java.nio.ByteBuffer

class GPUBufferDescriptorInit
@JvmOverloads constructor(
    /** The allowed usages for the buffer (e.g., vertex, uniform, copy_dst). */
    @BufferUsage.Type var usage: Int,

    /** Contents of a buffer on creation. */
    var content: ByteBuffer,

    /** The label for the buffer. */
    var label: String? = null,
)

/**
 * Creates [GPUBuffer] mapped with [GPUBufferDescriptorInit.content]
 */
fun GPUDevice.createBufferInit(descriptor: GPUBufferDescriptorInit): GPUBuffer {
    val buffer = createBuffer(
        GPUBufferDescriptor(
            size = descriptor.content.paddedSize,
            label = descriptor.label,
            usage = descriptor.usage,
            mappedAtCreation = true
        )
    )

    val mapped = buffer.getMappedRange()
    mapped.put(descriptor.content)
    buffer.unmap()
    return buffer
}