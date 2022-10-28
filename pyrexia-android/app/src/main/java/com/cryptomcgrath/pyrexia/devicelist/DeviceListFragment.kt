package com.cryptomcgrath.pyrexia.devicelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cryptomcgrath.pyrexia.databinding.FragmentDeviceListBinding
import com.cryptomcgrath.pyrexia.model.PyDevice

internal class DeviceListFragment : Fragment() {
    private val viewModel: DeviceListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is DeviceListEvent.GoToStatList -> {
                    goToStatListFragment(event.pyDevice)
                }
                is DeviceListEvent.GoToDeviceConfig -> {
                    gotoDeviceConfigFragment(event.pyDevice)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        binding.model = viewModel
        binding.recyclerView.adapter = DeviceListAdapter(viewModel.store, viewModel.dispatcher)
        binding.fab.setOnClickListener {
            viewModel.onClickAdd()
        }
        return binding.root
    }

    private fun goToStatListFragment(pyDevice: PyDevice) {
        val action = DeviceListFragmentDirections.actionDeviceListFragmentToStatListFragment(pyDevice, pyDevice.name)
        findNavController().navigate(action)
    }

    private fun gotoDeviceConfigFragment(pyDevice: PyDevice) {
        val action = DeviceListFragmentDirections.actionDeviceListFragmentToDeviceConfigFragment(pyDevice, pyDevice.name)
        findNavController().navigate(action)
    }
}