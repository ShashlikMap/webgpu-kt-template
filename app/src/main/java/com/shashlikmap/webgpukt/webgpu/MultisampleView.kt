package com.shashlikmap.webgpukt.webgpu

import androidx.webgpu.GPUDevice
import androidx.webgpu.GPUExtent3D
import androidx.webgpu.GPUMultisampleState
import androidx.webgpu.GPUSurfaceConfiguration
import androidx.webgpu.GPUTextureDescriptor
import androidx.webgpu.GPUTextureView
import androidx.webgpu.TextureDimension
import androidx.webgpu.TextureUsage

class MultisampleView(
    device: GPUDevice,
    gpuSurfaceConfiguration: GPUSurfaceConfiguration
) {
    companion object Companion {
        private const val SAMPLE_COUNT = 4
    }

    val textureView: GPUTextureView

    val multisampleState: GPUMultisampleState = GPUMultisampleState(count = SAMPLE_COUNT)

    init {
        val extent = GPUExtent3D(
            width = gpuSurfaceConfiguration.width,
            height = gpuSurfaceConfiguration.height,
        )

        val textureDescriptor = GPUTextureDescriptor(
            usage = TextureUsage.RenderAttachment,
            size = extent,
            dimension = TextureDimension._2D,
            format = gpuSurfaceConfiguration.format,
            mipLevelCount = 1,
            sampleCount = SAMPLE_COUNT,
        )

        textureView = device.createTexture(textureDescriptor).createView()
    }
}