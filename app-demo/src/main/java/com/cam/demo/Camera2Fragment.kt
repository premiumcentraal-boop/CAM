package com.cam.demo

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.cam.demo.databinding.FragmentCamera2Binding

/**
 * Camera2Fragment — baseline (real-camera) path, structured with EXACTLY two
 * choke-points where the Run 4 hook will later intercept:
 *   1. CameraManager.openCamera()  (device selection)
 *   2. onImageAvailable()/SurfaceTexture frame arrival (frame source)
 * No other part of this file may need changing in Run 4.
 */
class Camera2Fragment : Fragment(R.layout.fragment_camera2) {

    private var cameraDevice: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var captureRequest: CaptureRequest.Builder? = null
    private var imageReader: ImageReader? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null

    private var backgroundThread: android.os.HandlerThread? = null
    private var backgroundHandler: android.os.Handler? = null

    private val cameraManager: CameraManager
        get() = requireContext().getSystemService(CameraManager::class.java)

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentCamera2Binding.bind(view)
        binding.closeButton.setOnClickListener { openCamera(binding) }
        openCamera(binding)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(binding: FragmentCamera2Binding) {
        backgroundThread = android.os.HandlerThread("cam2").apply { start() }
        backgroundHandler = android.os.Handler(backgroundThread!!.looper)

        // CHOKE-POINT 1: device selection — Run 4 redirects this to the persona camera.
        val cameraId = cameraManager.cameraIdList.firstOrNull() ?: run {
            binding.statusText.text = getString(R.string.no_camera); return
        }

        val texture = SurfaceTexture(0).apply {
            setDefaultBufferSize(1920, 1080)
        }.also { surfaceTexture = it }
        surface = Surface(texture)

        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 2).apply {
            // CHOKE-POINT 2: frame arrival — Run 4 swaps the producer upstream.
            setOnImageAvailableListener({ reader ->
                reader.acquireLatestImage()?.use { img ->
                    val buffer = img.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
                    binding.statusText.post {
                        binding.statusText.text = getString(R.string.captured_bytes, bytes.size)
                    }
                }
            }, backgroundHandler)
        }

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                createSession(binding)
            }
            override fun onDisconnected(d: CameraDevice) { d.close(); cameraDevice = null }
            override fun onError(d: CameraDevice, error: Int) {
                binding.statusText.text = getString(R.string.camera_error, error)
            }
        }, backgroundHandler)
    }

    private fun createSession(binding: FragmentCamera2Binding) {
        val device = cameraDevice ?: return
        texture?.setOnFrameAvailableListener {
            binding.textureView.post { binding.textureView.invalidate() }
        }
        device.createCaptureSession(
            listOfNotNull(surface, imageReader?.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(s: CameraCaptureSession) {
                    session = s
                    captureRequest = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                        addTarget(surface!!)
                        set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)
                    }
                    session?.setRepeatingRequest(captureRequest!!.build(), null, backgroundHandler)
                }
                override fun onConfigureFailed(s: CameraCaptureSession) {}
            },
            backgroundHandler
        )
    }

    override fun onStop() {
        super.onStop()
        runCatching {
            session?.close(); cameraDevice?.close();
            imageReader?.close(); surfaceTexture?.release(); surface?.release()
        }
        backgroundThread?.quitSafely()
        session = null; cameraDevice = null
    }
}
