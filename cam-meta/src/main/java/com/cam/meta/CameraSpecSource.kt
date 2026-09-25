package com.cam.meta

import com.cam.core.persona.Persona

/**
 * CameraSpecSource — seam for the Run 6 Meta Engine.
 *
 * Run 4's hook asks THIS interface what CameraCharacteristics should report.
 * v0 placeholder returns persona-derived basics; Run 6 replaces the backing
 * implementation with real device-dump data. The contract is final today.
 */
interface CameraSpecSource {
    fun specFor(personaId: String): CameraSpec?
}

/** v0 spec fields — the minimal coherent set. Run 6 expands to full characteristics. */
data class CameraSpec(
    val sensorOrientation: Int,       // 0/90/180/270
    val maxJpegWidth: Int,
    val maxJpegHeight: Int,
    val focalLengthPixels: Float,     // derived from 35mm-equiv + sensor size in dump
    val availableApertures: List<Float>,
    val hardwareLevel: Int            // CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
)

/** Convenience for the placeholder phase. */
object SimpleSpecSource : CameraSpecSource {
    override fun specFor(personaId: String): CameraSpec? = null  // Run 6 populates
}

/** Helper mirroring what Run 4 will need, derived from Persona. */
fun specFromPersona(p: Persona): CameraSpec = CameraSpec(
    sensorOrientation = p.cameraSensorOrientation,
    maxJpegWidth = p.cameraMaxJpegWidth,
    maxJpegHeight = p.cameraMaxJpegHeight,
    focalLengthPixels = 3.4f,          // placeholder — dump-derived in Run 6
    availableApertures = listOf(1.8f),
    hardwareLevel = 2                  // FULL — conservative real-device default
)
