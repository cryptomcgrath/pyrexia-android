package com.cryptomcgrath.pyrexia.thermostat

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
import com.cryptomcgrath.pyrexia.databinding.FragmentThermostatBinding
import com.cryptomcgrath.pyrexia.deviceconfig.createNetworkErrorAlertDialog


internal class ThermostatFragment: Fragment() {
    private val args: ThermostatFragmentArgs by navArgs()
    private val central get() = CentralStore.getInstance(requireActivity().application)

    private val viewModel: ThermostatViewModel by viewModels {
        ThermostatViewModel.Factory(
            repo = central,
            dispatcher = central.dispatcher,
            store = central.store,
            pyDevice = args.pydevice,
            stat = args.stat)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.eventQueue.handleEvents(this) { event ->
            when(event) {
                is ThermostatEvent.ShowNetworkError -> {
                    showNetworkError(event.throwable, event.finish)
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
                dispatcher = central.dispatcher,
                pyDevice = viewModel.pyDevice,
                store = central.store,
                statId = viewModel.stat.program.id
            )
        }
        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbar
            .setupWithNavController(findNavController(), appBarConfiguration)
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelAutoRefresh()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStats()
        // TODO: causing network error
        //val elapsed = viewModel.stat.lastRefreshTimeSecs - (System.currentTimeMillis() / 1000)
        //central.dispatcher.post(
        //    ThermostatEvent.RequestHistoryBefore(
        //       viewModel.pyDevice, viewModel.stat.program.id, (viewModel.stat.currentTimeSecs ?: 0 + elapsed).toInt() ))
    }

    private fun showNetworkError(throwable: Throwable, finish: Boolean) {
        createNetworkErrorAlertDialog(requireContext(), throwable) {
            if (finish) findNavController().popBackStack()
        }.show()
    }
}