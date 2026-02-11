package com.shashlikmap.webgpukt.webgpu

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shashlikmap.webgpukt.mesh.Mesh

@Composable
fun WebGPUView(mesh: Mesh) {
    val api = remember { WebGpuAPI(mesh) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event >= Lifecycle.Event.ON_STOP) {
                api.reset()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            api.reset()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    api.rotationAngle -= dragAmount.x / 5.0f
                }
            }) {
        AndroidView(
            factory = { context ->
                WebGPUSurfaceView(context, api)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}