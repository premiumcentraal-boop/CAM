package com.cam.demo

import android.hardware.Camera
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Camera1Fragment — baseline legacy-API path (API 1).
 * KYC SDKs love this API; Run 3 hooks open()+setPreviewDisplay()+takePicture().
 * Structured so the hook intercepts without altering this file.
 */
@Suppress("DEPRECATION")
class Camera1Fragment : Fragment(R.layout.fragment_camera1) {

    private var camera: Camera? = null

    private val holderCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) { /* opened in resume flow */ }
        override fun surfaceChanged(holder: SurfaceHolder, fmt: Int, w: Int, h: Int) {
            startPreview(holder)
        }
        override fun surfaceDestroyed(holder: SurfaceHolder) { releaseCamera() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val holder = view.findViewById<SurfaceView>(R.id.surface_view).holder
        holder.addCallback(holderCallback)

        view.findViewById<Button>(R.id.capture_button).setOnClickListener {
            camera?.takePicture(null, null, null, { data ->
                view.findViewById<TextView>(R.id.status_text).text =
                    getString(R.string.captured_bytes, data.size)
                camera?.startPreview()
            })
        }
    }

    private fun startPreview(holder: SurfaceHolder) {
        releaseCamera()
        // CHOKE-POINT: Camera.open() — Run 3 intercepts here.
        camera = Camera.open(0).apply {
            setPreviewDisplay(holder)
            parameters = parameters.apply {
                previewSize?.let { setPreviewSize(it.width, it.height) }
            }
            startPreview()
        }
    }

    private fun releaseCamera() {
        camera?.run { release(); }
        camera = null
    }

    override fun onStop() = releaseCamera().let { }
}
