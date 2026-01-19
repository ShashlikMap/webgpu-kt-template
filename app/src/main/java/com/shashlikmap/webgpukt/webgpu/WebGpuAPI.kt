package com.shashlikmap.webgpukt.webgpu

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Matrix
import android.view.Surface
import androidx.webgpu.BufferBindingType
import androidx.webgpu.BufferUsage
import androidx.webgpu.CullMode
import androidx.webgpu.FrontFace.Companion.CW
import androidx.webgpu.GPU
import androidx.webgpu.GPUAdapter
import androidx.webgpu.GPUBindGroup
import androidx.webgpu.GPUBindGroupDescriptor
import androidx.webgpu.GPUBindGroupEntry
import androidx.webgpu.GPUBindGroupLayout
import androidx.webgpu.GPUBindGroupLayoutDescriptor
import androidx.webgpu.GPUBindGroupLayoutEntry
import androidx.webgpu.GPUBlendState
import androidx.webgpu.GPUBuffer
import androidx.webgpu.GPUBufferBindingLayout
import androidx.webgpu.GPUBufferDescriptor
import androidx.webgpu.GPUColor
import androidx.webgpu.GPUColorTargetState
import androidx.webgpu.GPUDevice
import androidx.webgpu.GPUFragmentState
import androidx.webgpu.GPUInstance
import androidx.webgpu.GPUInstanceDescriptor
import androidx.webgpu.GPUInstanceLimits
import androidx.webgpu.GPUPipelineLayoutDescriptor
import androidx.webgpu.GPUPrimitiveState
import androidx.webgpu.GPURenderPassColorAttachment
import androidx.webgpu.GPURenderPassDescriptor
import androidx.webgpu.GPURenderPipeline
import androidx.webgpu.GPURenderPipelineDescriptor
import androidx.webgpu.GPUShaderModuleDescriptor
import androidx.webgpu.GPUShaderSourceWGSL
import androidx.webgpu.GPUSurface
import androidx.webgpu.GPUSurfaceConfiguration
import androidx.webgpu.GPUSurfaceDescriptor
import androidx.webgpu.GPUSurfaceSourceAndroidNativeWindow
import androidx.webgpu.GPUVertexBufferLayout
import androidx.webgpu.GPUVertexState
import androidx.webgpu.IndexFormat
import androidx.webgpu.InstanceFeatureName
import androidx.webgpu.LoadOp
import androidx.webgpu.ShaderStage
import androidx.webgpu.StoreOp
import androidx.webgpu.VertexFormat.Companion.Float32x3
import androidx.webgpu.VertexFormat.Companion.Float32x4
import androidx.webgpu.VertexStepMode
import androidx.webgpu.helper.Util
import com.shashlikmap.webgpukt.R
import com.shashlikmap.webgpukt.mesh.Mesh
import com.shashlikmap.webgpukt.webgpu.utils.GPUBufferDescriptorInit
import com.shashlikmap.webgpukt.webgpu.utils.alphaBlending
import com.shashlikmap.webgpukt.webgpu.utils.byteSize
import com.shashlikmap.webgpukt.webgpu.utils.createBufferInit
import com.shashlikmap.webgpukt.webgpu.utils.gpuVertexAttributes
import com.shashlikmap.webgpukt.webgpu.utils.paddedSize
import com.shashlikmap.webgpukt.webgpu.utils.toByteBuffer
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.rotation

/**
 * Wrapper around androidx.webgpu
 */
@SuppressLint("RestrictedApi")
class WebGpuAPI(private val mesh: Mesh) {
    companion object {
        const val WEBGPU_C_BUNDLED = "webgpu_c_bundled"

        val GL_TO_WGPU = floatArrayOf(
            1.0f,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            1.0f,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            0.5f,
            0.5f,
            0.0f,
            0.0f,
            0.0f,
            1.0f
        )

        val INITIAL_EYE = Float4(0.0f, 2.0f, 3.0f, 1.0f)

        // TODO Code gen to struct prototype?
        val SHADER_STRUCT = arrayOf(Float32x3, Float32x3, Float32x4)

        init {
            System.loadLibrary(WEBGPU_C_BUNDLED)
        }
    }

    private var gpuInstance: GPUInstance? = null
    private var gpuAdapter: GPUAdapter? = null
    private var gpuDevice: GPUDevice? = null
    private var gpuSurface: GPUSurface? = null
    private var gpuRenderPipeline: GPURenderPipeline? = null
    private var isPrepared = false

    var rotationAngle = 0.0f

    private var meshVertexBuffer: GPUBuffer? = null
    private var meshIndexBuffer: GPUBuffer? = null
    private var globalUniformBuffer: GPUBuffer? = null
    private var globalUniformBindGroupLayout: GPUBindGroupLayout? = null
    private var globalUniformBindGroup: GPUBindGroup? = null

    private val perspectiveMatrix = FloatArray(16)
    private val viewProjMatrix = FloatArray(16)

    private suspend fun prepare() {
        if (isPrepared) return

        val instanceDescriptor = GPUInstanceDescriptor(
            requiredFeatures = intArrayOf(InstanceFeatureName.TimedWaitAny), requiredLimits =
                GPUInstanceLimits()
        )
        gpuInstance = GPU.createInstance(instanceDescriptor)
        gpuAdapter = gpuInstance?.requestAdapter()
        gpuDevice = gpuAdapter?.requestDevice()

        createGlobalUniform()
        createMeshBuffers()

        isPrepared = true
    }

    suspend fun setSurface(context: Context, nativeSurface: Surface, width: Int, height: Int) {
        prepare()

        val nativeWindow = Util.windowFromSurface(surface = nativeSurface)

        val surfaceDescriptor = GPUSurfaceDescriptor(
            surfaceSourceAndroidNativeWindow = GPUSurfaceSourceAndroidNativeWindow(nativeWindow)
        )
        gpuSurface = gpuInstance?.createSurface(descriptor = surfaceDescriptor)

        configureSurface(width, height)
        createRenderPipeline(context)
    }

    private fun configureSurface(width: Int, height: Int) {
        val currentSurface = gpuSurface ?: return
        val currentAdapter = gpuAdapter ?: return
        val currentDevice = gpuDevice ?: return

        Matrix.perspectiveM(
            perspectiveMatrix,
            0,
            45f,
            width.toFloat() / height.toFloat(),
            0.1f,
            100f
        )

        val capabilities = currentSurface.getCapabilities(currentAdapter)
        val textureFormat = capabilities.formats.firstOrNull() ?: return
        val surfaceConfiguration = GPUSurfaceConfiguration(
            device = currentDevice,
            format = textureFormat,
            width = width,
            height = height
        )
        currentSurface.configure(surfaceConfiguration)
    }

    private fun createRenderPipeline(context: Context) {
        val currentDevice = gpuDevice ?: return
        val currentSurface = gpuSurface ?: return
        val currentAdapter = gpuAdapter ?: return

        val shader = context.resources.openRawResource(R.raw.shader).bufferedReader().readText()
        val shaderModuleDescriptor = GPUShaderModuleDescriptor().apply {
            shaderSourceWGSL = GPUShaderSourceWGSL(shader)
        }

        val module = currentDevice.createShaderModule(shaderModuleDescriptor)

        val capabilities = currentSurface.getCapabilities(currentAdapter)
        val textureFormat = capabilities.formats.firstOrNull() ?: return

        val colorTargetState = GPUColorTargetState(
            format = textureFormat,
            blend = GPUBlendState().alphaBlending
        )
        val fragmentState = GPUFragmentState(
            module = module,
            targets = arrayOf(colorTargetState),
            entryPoint = "fragmentMain"
        )

        val vertexBufferLayout = GPUVertexBufferLayout(
            arrayStride = SHADER_STRUCT.byteSize,
            stepMode = VertexStepMode.Vertex,
            attributes = SHADER_STRUCT.gpuVertexAttributes
        )

        val renderPipelineDescriptor = GPURenderPipelineDescriptor(
            vertex = GPUVertexState(
                module = module,
                entryPoint = "vertexMain",
                buffers = arrayOf(vertexBufferLayout)
            ),
            fragment = fragmentState,
            primitive = GPUPrimitiveState(frontFace = CW, cullMode = CullMode.Back),
            layout = currentDevice.createPipelineLayout(
                GPUPipelineLayoutDescriptor(
                    bindGroupLayouts = arrayOf(globalUniformBindGroupLayout!!)
                )
            )
        )

        gpuRenderPipeline = currentDevice.createRenderPipeline(renderPipelineDescriptor)
    }

    private fun createGlobalUniform() {
        val currentDevice = gpuDevice ?: return

        globalUniformBuffer =
            currentDevice.createBuffer(
                GPUBufferDescriptor(
                    size = viewProjMatrix.paddedSize,
                    usage = BufferUsage.CopyDst or BufferUsage.Uniform
                )
            )

        globalUniformBindGroupLayout = currentDevice.createBindGroupLayout(
            GPUBindGroupLayoutDescriptor(
                entries = arrayOf(
                    GPUBindGroupLayoutEntry(
                        binding = 0,
                        visibility = ShaderStage.Vertex,
                        buffer = GPUBufferBindingLayout(type = BufferBindingType.Uniform)
                    )
                )
            )
        )

        globalUniformBindGroup = currentDevice.createBindGroup(
            descriptor = GPUBindGroupDescriptor(
                layout = globalUniformBindGroupLayout!!,
                entries = arrayOf(GPUBindGroupEntry(binding = 0, buffer = globalUniformBuffer!!))
            )
        )
    }

    private fun createMeshBuffers() {
        val currentDevice = gpuDevice ?: return

        meshVertexBuffer = currentDevice.createBufferInit(
            GPUBufferDescriptorInit(
                usage = BufferUsage.Vertex,
                content = mesh.vertices.toByteBuffer(),
            )
        )

        meshIndexBuffer = currentDevice.createBufferInit(
            GPUBufferDescriptorInit(
                usage = BufferUsage.Index,
                content = mesh.indices.toIntArray().toByteBuffer(),
            )
        )
    }

    fun update() {
        val currentDevice = gpuDevice ?: return
        globalUniformBuffer ?: return

        updateViewProjMatrix(rotationAngle)

        currentDevice.queue.writeBuffer(
            buffer = globalUniformBuffer!!,
            bufferOffset = 0,
            data = viewProjMatrix.toByteBuffer()
        )
    }

    private fun updateViewProjMatrix(rotationAngle: Float) {
        val rotationMatrix = rotation(d = Float3(y = rotationAngle))
        val finalEye = rotationMatrix * INITIAL_EYE
        Matrix.setLookAtM(
            viewProjMatrix,
            0,
            finalEye.x,
            finalEye.y,
            finalEye.z,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            1.0f,
            0.0f
        )
        Matrix.multiplyMM(viewProjMatrix, 0, perspectiveMatrix, 0, viewProjMatrix, 0)
        Matrix.multiplyMM(viewProjMatrix, 0, GL_TO_WGPU, 0, viewProjMatrix, 0)
    }

    fun render() {
        val currentSurface = gpuSurface ?: return
        val currentDevice = gpuDevice ?: return
        val currentRenderPipeline = gpuRenderPipeline ?: return

        val surfaceTexture = currentSurface.getCurrentTexture()
        val renderPassColorAttachment = GPURenderPassColorAttachment(
            view = surfaceTexture.texture.createView(),
            loadOp = LoadOp.Clear,
            storeOp = StoreOp.Store,
            clearValue = GPUColor(0.0, 0.0, 0.5, 1.0) // Blue background for visibility
        )

        val renderPassDescriptor = GPURenderPassDescriptor(
            colorAttachments = arrayOf(renderPassColorAttachment)
        )

        val commandEncoder = currentDevice.createCommandEncoder()
        val renderPassEncoder = commandEncoder.beginRenderPass(renderPassDescriptor)

        renderPassEncoder.apply {
            setPipeline(currentRenderPipeline)
            setBindGroup(0, globalUniformBindGroup!!)
            setVertexBuffer(0, meshVertexBuffer!!)
            setIndexBuffer(meshIndexBuffer!!, format = IndexFormat.Uint32)
            drawIndexed(mesh.indices.size)
            end()
        }

        val commands = commandEncoder.finish()

        currentDevice.queue.submit(arrayOf(commands))

        currentSurface.present()
        gpuInstance?.processEvents()
    }

    fun reset() {
        gpuSurface = null
        gpuRenderPipeline = null
        gpuDevice = null
        gpuAdapter = null
        gpuInstance = null
        isPrepared = false
    }
}