package com.cryptomcgrath.pyrexia

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity: FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        goToThermostatFragment()
    }

    private fun goToThermostatFragment() {
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, ThermostatFragment()).commit()
    }

}