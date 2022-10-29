package com.cryptomcgrath.pyrexia.deviceconfig

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cryptomcgrath.pyrexia.databinding.FragmentDeviceConfigBinding

internal class DeviceConfigFragment: Fragment() {
    private val args: DeviceConfigFragmentArgs by navArgs()

    private val viewModel: DeviceConfigViewModel by viewModels {
        DeviceConfigViewModel.Factory(pyDevice = args.pyDevice)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDeviceConfigBinding.inflate(inflater, container, false)
        binding.model = viewModel
        binding.recyclerView.adapter = DeviceConfigAdapter(
            context = requireContext(),
            store = viewModel.store,
            dispatcher = viewModel.dispatcher
        )
        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbar
            .setupWithNavController(findNavController(), appBarConfiguration)
        return binding.root
    }

}