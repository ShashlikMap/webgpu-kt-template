# webgpu-kt-template
A complete Android app example/template that uses [androidx.webgpu](https://developer.android.com/jetpack/androidx/releases/webgpu) library.

The repository serves as a starting point for Android developers who want to explore using WebGPU for high-performance graphics and computation within a familiar Kotlin/Android environment. WebGPU is designed to be the modern successor to older APIs like OpenGL ES.

This example uses the latest [1.0.0-alpha03 version](https://developer.android.com/jetpack/androidx/releases/webgpu#1.0.0-alpha03). It renders a simple cube, a camera can be rotated around using touch gesture. It also demostrates how to work with:
- WebGPU configuration + integration with Compose UI
- Matrices calculation
- Creating GPU buffers(vertex/index), both for uniform and vertices purposes
- Simple diffuse lighting
- A few [tweaks](https://hackmd.io/@agent10/r16ntkfSbg) around androidx.webgpu
- _Antialiasing + depth features are in progress_

<img width="200" alt="Screenshot_20260119_223639" src="https://github.com/user-attachments/assets/c2dcd3c4-76cc-4022-9109-fbdb6ac5ceb5" />

I recommend to install a couple plugins for Android Studio :
- [WGSL plugin](https://plugins.jetbrains.com/plugin/18110-wgsl-support) if you want to play with shaders.
- [Wavefront OBJ viewer](https://plugins.jetbrains.com/plugin/14843-wavefront-obj) helps to render the OBJ format right from the Studio.

P.S. Thanks to the [article](https://blog.androiddevapps.com/posts/getting-started-with-webgpu-on-android/) for the inspiration.

P.S.S. Compose Multiplatform + Rust WGPU template can be found [here](https://github.com/ShashlikMap/WgpuKmp-Template)
