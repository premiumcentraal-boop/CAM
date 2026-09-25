package com.cam.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cam.core.persona.Persona
import com.cam.core.persona.PersonaLoader
import com.cam.core.persona.PersonaValidator
import com.cam.demo.databinding.FragmentProbeBinding
import org.json.JSONObject
import java.io.File

/**
 * ProbeFragment — the regression-oracle seed.
 * Dumps every camera characteristic the platform exposes as schema'd JSON.
 * Runs 4/6 will diff this dump (hooked vs real) to prove bit-consistency.
 */
class ProbeFragment : Fragment(R.layout.fragment_probe) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentProbeBinding.bind(view)
        val ctx = requireContext()

        val pm = ctx.packageManager
        val pkgInfo = pm.getPackageInfo(ctx.packageName, 0)

        val dump = JSONObject().apply {
            put("app", JSONObject()
                .put("versionName", pkgInfo.versionName ?: "?")
                .put("sdkInt", android.os.Build.VERSION.SDK_INT))
            put("build", JSONObject()
                .put("model", android.os.Build.MODEL)
                .put("manufacturer", android.os.Build.MANUFACTURER)
                .put("fingerprint", android.os.Build.FINGERPRINT)
                .put("securityPatch", android.os.Build.VERSION.SECURITY_PATCH))
            put("cameras", CameraProbe.dumpAll(ctx))
        }

        binding.probeText.text = dump.toString(2)

        // Persist so adb-pull + CI diffing work identically to on-device runs
        runCatching {
            val dir = File(ctx.getExternalFilesDir(null), "probes").apply { mkdirs() }
            File(dir, "probe_${System.currentTimeMillis()}.json").writeText(dump.toString(2))
            binding.probeStatus.text = getString(R.string.probe_saved, dir.absolutePath)
        }.onFailure {
            binding.probeStatus.text = "persist failed: ${it.message}"
        }

        // Show currently-loaded sample persona as sanity check of cam-core wiring
        val persona = samplePersona()
        val issues = PersonaValidator.validate(persona)
        binding.personaStatus.text = if (issues.isEmpty()) {
            "persona OK: ${persona.personaId} (${persona.deviceModel})"
        } else "persona INVALID: $issues"
    }

    private fun samplePersona(): Persona = PersonaLoader.fromJson(SAMPLE_PERSONA_JSON)

    companion object {
        // Example persona for the smoke test; real generator lands in Run 1.
        const val SAMPLE_PERSONA_JSON = """{
            "personaId": "nl-rotterdam-001",
            "deviceModel": "SM-A546B",
            "manufacturer": "samsung",
            "androidVersion": 34,
            "securityPatch": "2024-06-01",
            "cameraSensorOrientation": 90,
            "cameraMaxJpegWidth": 4096,
            "cameraMaxJpegHeight": 3072,
            "feedImagePath": "personas/nl-rotterdam-001/portrait.jpg",
            "noiseSeed": 4172909
        }"""
    }
}
