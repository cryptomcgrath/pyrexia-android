package com.cryptomcgrath.pyrexia.deviceconfig

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.databinding.FragmentDeviceConfigBinding
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor

internal class DeviceConfigFragment: Fragment() {
    private val args: DeviceConfigFragmentArgs by navArgs()

    private val viewModel: DeviceConfigViewModel by activityViewModels {
        DeviceConfigViewModel.Factory(pyDevice = args.pyDevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is DeviceConfigEvent.GoToSensorEdit -> {
                    goToSensorEditDialog(args.pyDevice, event.sensor)
                }

                is DeviceConfigEvent.ServicesError -> {
                    showServicesError(event.throwable)
                }
            }
        }
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

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    private fun goToSensorEditDialog(pyDevice: PyDevice,
                                     sensor: Sensor) {
        val action = DeviceConfigFragmentDirections.actionDeviceConfigFragmentToSensorEditBottomSheetFragment(pyDevice, sensor)
        findNavController().navigate(action)
    }

    private fun showServicesError(throwable: Throwable) {
        AlertDialog.Builder(requireActivity())
            .setPositiveButton(R.string.ok) { di, _ ->
                di.dismiss()
                findNavController().popBackStack()
            }
            .setTitle("Service Error")
            .setMessage(throwable.toString())
            .create().show()
    }

}