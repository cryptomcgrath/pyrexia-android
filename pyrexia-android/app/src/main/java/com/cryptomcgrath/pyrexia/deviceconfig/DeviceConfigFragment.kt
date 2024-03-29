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
import com.cryptomcgrath.pyrexia.CentralStore
import com.cryptomcgrath.pyrexia.databinding.FragmentDeviceConfigBinding
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.model.VirtualStat

internal class DeviceConfigFragment: Fragment() {
    private val args: DeviceConfigFragmentArgs by navArgs()
    private val central: CentralStore get() = CentralStore.getInstance(requireActivity().application)

    private val viewModel: DeviceConfigViewModel by viewModels {
        DeviceConfigViewModel.Factory(
            repo = central,
            store = central.store,
            dispatcher = central.dispatcher,
            pyDevice = args.pyDevice
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is DeviceConfigEvent.ShowNetworkError -> {
                    createNetworkErrorAlertDialog(requireContext(), event.throwable) {
                        if (event.finish) {
                            findNavController().popBackStack()
                        }
                    }.show()
                }

                is DeviceConfigEvent.OnShutdownCompleted -> {
                    findNavController().popBackStack()
                }

                is DeviceConfigEvent.GoToSensorEdit -> {
                    goToSensorEditDialog(args.pyDevice, event.sensor)
                }

                is DeviceConfigEvent.GoToControlEdit -> {
                    goToControlEditDialog(args.pyDevice, event.control)
                }

                is DeviceConfigEvent.GoToStatEdit -> {
                    goToStatEditDialog(args.pyDevice, event.stat)
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
                        Component.VSTAT -> {
                            goToStatEditDialog(
                                pyDevice = args.pyDevice,
                                stat = VirtualStat()
                            )
                        }
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
            pyDevice = viewModel.pyDevice,
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
        viewModel.refreshDeviceConfig()
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

    private fun goToAddComponent(pyDevice: PyDevice) {
        val action = DeviceConfigFragmentDirections.actionDeviceConfigFragmentToAddComponentBottomSheetFragment(pyDevice)
        findNavController().navigate(action)
    }

    private fun goToStatEditDialog(pyDevice: PyDevice, stat: VirtualStat) {
        val action = DeviceConfigFragmentDirections.actionDeviceConfigFragmentToStatEditBottomSheetFragment(pyDevice, stat)
        findNavController().navigate(action)
    }
}