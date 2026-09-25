package com.cam.core.persona

/**
 * Persona — the atomic identity record. Schema v0 (Run 1 expands).
 *
 * Every future subsystem (feed, hooks, meta, EXIF, proxy) reads from this
 * single object so no two components can disagree about who the persona is.
 */
data class Persona(
    val personaId: String,
    val deviceModel: String,          // e.g. "SM-A546B"
    val manufacturer: String,         // e.g. "samsung"
    val androidVersion: Int,          // SDK int, e.g. 34
    val securityPatch: String,        // e.g. "2024-06-01"
    // camera-related identity (Meta Engine expands in Run 6)
    val cameraSensorOrientation: Int = 90,
    val cameraMaxJpegWidth: Int = 4000,
    val cameraMaxJpegHeight: Int = 3000,
    // feed assets
    val feedVideoPath: String? = null,
    val feedImagePath: String? = null,
    val noiseSeed: Long = 0L
) {
    init {
        require(personaId.isNotBlank()) { "personaId required" }
        require(androidVersion >= 30) { "Android 11+ only (v1 scope)" }
    }
}
