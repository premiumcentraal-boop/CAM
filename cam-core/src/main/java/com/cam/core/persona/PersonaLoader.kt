package com.cam.core.persona

import org.json.JSONObject

/** Loads Persona from JSON. Fail-fast on malformed input; never half-constructs. */
object PersonaLoader {

    fun fromJson(json: String): Persona {
        val o = JSONObject(json)
        return Persona(
            personaId = o.getString("personaId"),
            deviceModel = o.optString("deviceModel", ""),
            manufacturer = o.optString("manufacturer", ""),
            androidVersion = o.optInt("androidVersion", 34),
            securityPatch = o.optString("securityPatch", ""),
            cameraSensorOrientation = o.optInt("cameraSensorOrientation", 90),
            cameraMaxJpegWidth = o.optInt("cameraMaxJpegWidth", 4000),
            cameraMaxJpegHeight = o.optInt("cameraMaxJpegHeight", 3000),
            feedVideoPath = if (o.has("feedVideoPath")) o.getString("feedVideoPath") else null,
            feedImagePath = if (o.has("feedImagePath")) o.getString("feedImagePath") else null,
            noiseSeed = o.optLong("noiseSeed", 0L)
        )
    }

    fun toJson(p: Persona): String = JSONObject().apply {
        put("personaId", p.personaId)
        put("deviceModel", p.deviceModel)
        put("manufacturer", p.manufacturer)
        put("androidVersion", p.androidVersion)
        put("securityPatch", p.securityPatch)
        put("cameraSensorOrientation", p.cameraSensorOrientation)
        put("cameraMaxJpegWidth", p.cameraMaxJpegWidth)
        put("cameraMaxJpegHeight", p.cameraMaxJpegHeight)
        p.feedVideoPath?.let { put("feedVideoPath", it) }
        p.feedImagePath?.let { put("feedImagePath", it) }
        put("noiseSeed", p.noiseSeed)
    }.toString(2)
}
