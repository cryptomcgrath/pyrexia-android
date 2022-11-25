package com.cryptomcgrath.pyrexia.deviceconfig

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cryptomcgrath.pyrexia.databinding.FragmentDeviceConfigBinding
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor

internal class DeviceConfigFragment: Fragment() {
    private val args: DeviceConfigFragmentArgs by navArgs()

    private val viewModel: DeviceConfigViewModel by activityViewModels {
        DeviceConfigViewModel.Factory(
            application = requireActivity().application,
            pyDevice = args.pyDevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is DeviceConfigEvent.GoToSensorEdit -> {
                    goToSensorEditDialog(args.pyDevice, event.sensor)
                }

                is DeviceConfigEvent.GoToControlEdit -> {
                    goToControlEditDialog(args.pyDevice, event.control)
                }

                is DeviceConfigEvent.NetworkError -> {
                    showNetworkError(event.throwable, event.finish)
                }

                is DeviceConfigEvent.OnComponentAddSelected -> {
                    when (event.component) {
                        Component.DHT22 -> {
                            goToSensorEditDialog(
                                pyDevice = args.pyDevice,
                                sensor = Sensor(
                                    sensorType = Sensor.SensorType.DHT22
                                )
                            )
                        }
                        Component.SENSORPUSH -> {
                            goToSensorEditDialog(
                                pyDevice = args.pyDevice,
                                sensor = Sensor(
                                    sensorType = Sensor.SensorType.SENSORPUSH
                                )
                            )
                        }
                        Component.RELAY -> {
                            goToControlEditDialog(
                                pyDevice = args.pyDevice,
                                control = Control()
                            )
                        }
                        else -> Unit
                    }
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
        binding.fab.setOnClickListener {
            goToAddComponent(args.pyDevice)
        }
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

    private fun goToControlEditDialog(pyDevice: PyDevice,
                                      control: Control) {
        val action = DeviceConfigFragmentDirections.actionDeviceConfigFragmentToControlEditBottomSheetFragment(pyDevice, control)
        findNavController().navigate(action)
    }

    private fun showNetworkError(throwable: Throwable, finish: Boolean) {
        createNetworkErrorAlertDialog(requireContext(), throwable) {
            if (finish) findNavController().popBackStack()
        }.show()
    }

    private fun goToAddComponent(pyDevice: PyDevice) {
        val action = DeviceConfigFragmentDirections.actionDeviceConfigFragmentToAddComponentBottomSheetFragment(pyDevice)
        findNavController().navigate(action)
    }
}