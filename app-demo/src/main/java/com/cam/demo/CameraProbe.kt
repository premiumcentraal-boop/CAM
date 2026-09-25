package com.cam.demo

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import org.json.JSONArray
import org.json.JSONObject

/** Static dump of every camera characteristic we will later spoof. */
object CameraProbe {

    fun dumpAll(context: Context): JSONArray {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val arr = JSONArray()
        for (id in cm.cameraIdList) {
            val c = cm.getCameraCharacteristics(id)
            arr.put(JSONObject().apply {
                put("id", id)
                put("facing", c.get(CameraCharacteristics.LENS_FACING) ?: -1)
                put("sensorOrientation", c.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: -1)
                put("hardwareLevel", c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ?: -1)

                (c.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE))?.let {
                    put("pixelArray", JSONObject().put("w", it.width).put("h", it.height))
                }
                c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let { m ->
                    put("jpegSizes", m.getOutputSizes(android.graphics.ImageFormat.JPEG)
                        ?.joinToString(",") { "${it.width}x${it.height}" } ?: "")
                }
                c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.let {
                    put("apertures", JSONArray(it.toList()))
                }
            })
        }
        return arr
    }
}
