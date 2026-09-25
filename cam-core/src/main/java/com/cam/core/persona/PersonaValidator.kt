package com.cam.core.persona

/**
 * Coherence validation — the rule that will save us from self-incriminating
 * personas: every field must agree with every other field. Run 1/6 expand
 * this dramatically (device-dump cross-checks); the skeleton rules live here.
 */
object PersonaValidator {

    /** @return list of violations; empty = valid */
    fun validate(p: Persona): List<String> {
        val issues = mutableListOf<String>()

        if (p.deviceModel.isBlank()) issues += "deviceModel blank"
        if (p.manufacturer.isBlank()) issues += "manufacturer blank"
        if (p.securityPatch.isNotBlank() && !REGEX_PATCH.matches(p.securityPatch)) {
            issues += "securityPatch not ISO date: ${p.securityPatch}"
        }
        if (p.cameraMaxJpegWidth <= 0 || p.cameraMaxJpegHeight <= 0) {
            issues += "camera JPEG dims must be positive"
        }
        if (p.cameraSensorOrientation !in listOf(0, 90, 180, 270)) {
            issues += "sensorOrientation must be 0/90/180/270"
        }
        if ((p.feedVideoPath == null) == (p.feedImagePath == null)) {
            issues += "exactly one of feedVideoPath / feedImagePath required"
        }
        return issues
    }

    fun isValid(p: Persona): Boolean = validate(p).isEmpty()

    private val REGEX_PATCH = Regex("\\d{4}-\\d{2}-\\d{2}")
}
