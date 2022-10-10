package com.cryptomcgrath.pyrexia.thermostat

import android.app.AlertDialog
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
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.databinding.FragmentThermostatBinding


internal class ThermostatFragment: Fragment() {
    private val args: ThermostatFragmentArgs by navArgs()

    private val viewModel: ThermostatViewModel by viewModels {
        ThermostatViewModel.Factory(args.pydevice, args.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is ThermostatViewModel.UiEvent.ServiceError -> {
                    showServicesError(event.throwable)
                }
                is ThermostatViewModel.UiEvent.StatEnable -> {
                    if (event.enable) {
                        viewModel.enableStat(event.id)
                    } else {
                        viewModel.disableStat(event.id)
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
        val binding = FragmentThermostatBinding.inflate(inflater, container, false).apply {
            model = viewModel
            recyclerView.adapter = ThermostatAdapter(
                context = requireContext(),
                dispatcher = viewModel.dispatcher,
                store = viewModel.store
            )
        }
        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbar
            .setupWithNavController(findNavController(), appBarConfiguration)
        return binding.root
    }

    private fun showServicesError(throwable: Throwable) {
        AlertDialog.Builder(requireActivity())
            .setPositiveButton(R.string.ok, null)
            .setTitle("Service Error")
            .setMessage(throwable.toString())
            .create().show()
    }
}