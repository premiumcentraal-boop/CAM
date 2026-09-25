package com.cam.demo

import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cam.demo.databinding.FragmentCameraXBinding
import com.google.common.util.concurrent.ListenableFuture

/**
 * CameraXFragment — baseline CameraX path. Run 5 hooks ProcessCameraProvider
 * internals; structure unchanged.
 */
class CameraXFragment : Fragment(R.layout.fragment_camerax) {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCameraXBinding.bind(view)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (_: Exception) { /* camera unavailable on emulator tier — tolerated */ }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
}
