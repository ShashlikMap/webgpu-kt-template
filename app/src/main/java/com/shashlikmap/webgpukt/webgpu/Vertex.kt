package com.shashlikmap.webgpukt.webgpu

import android.os.Parcelable
import com.shashlikmap.webgpukt.webgpu.utils.Vec3
import com.shashlikmap.webgpukt.webgpu.utils.Vec4
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vertex(val position: Vec3, val normal: Vec3, val color: Vec4) : Parcelable