package com.shashlikmap.webgpukt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.shashlikmap.webgpukt.mesh.MeshLoader
import com.shashlikmap.webgpukt.ui.theme.WebgpuKtTemplateTheme
import com.shashlikmap.webgpukt.webgpu.WebGPUView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mesh = MeshLoader.loadMesh(this, R.raw.cube)
        setContent {
            WebgpuKtTemplateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebGPUView(mesh)
                }
            }
        }
    }
}