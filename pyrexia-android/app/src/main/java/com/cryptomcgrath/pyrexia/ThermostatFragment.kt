package com.cryptomcgrath.pyrexia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cryptomcgrath.pyrexia.databinding.FragmentThermostatBinding


class ThermostatFragment: Fragment() {
    private val viewModel: ThermostatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentThermostatBinding.inflate(inflater, container, true).apply {
            model = viewModel
        }.root
    }
}