package com.cam.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cam.demo.databinding.ActivityMainBinding

/**
 * MainActivity — surface-flipper between the four capture-path demos.
 * Each path is a Fragment so hook tests later exercise identical UI structure
 * with and without XPOSED active.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) show(Camera2Fragment())

        binding.navGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.nav_api1 -> show(Camera1Fragment())
                R.id.nav_api2 -> show(Camera2Fragment())
                R.id.nav_camerax -> show(CameraXFragment())
                R.id.nav_probe -> show(ProbeFragment())
            }
        }
    }

    private fun show(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, f)
            .commit()
    }
}
