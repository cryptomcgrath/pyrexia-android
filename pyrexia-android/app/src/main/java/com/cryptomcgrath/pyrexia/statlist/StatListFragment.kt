package com.cryptomcgrath.pyrexia.statlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cryptomcgrath.pyrexia.CentralStore
import com.cryptomcgrath.pyrexia.databinding.FragmentStatListBinding
import com.cryptomcgrath.pyrexia.deviceconfig.createNetworkErrorAlertDialog
import com.cryptomcgrath.pyrexia.thermostat.ThermostatFragmentDirections

internal class StatListFragment: Fragment() {
    private val args: StatListFragmentArgs by navArgs()
    private val central get() = CentralStore.getInstance(requireActivity().application)

    private val viewModel: StatListViewModel by viewModels {
        StatListViewModel.Factory(repo = central, dispatcher = central.dispatcher, pyDevice = args.pydevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is StatListEvent.OnStatSelected -> {
                    val action = ThermostatFragmentDirections.actionGlobalThermostatFragment(
                        stat = event.stat,
                        name = event.name,
                        pydevice = args.pydevice)
                    findNavController().navigate(action)
                }

                is StatListEvent.ShowNetworkError -> {
                    createNetworkErrorAlertDialog(requireContext(), event.throwable) {
                        if (event.finish) findNavController().popBackStack()
                    }.show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStatListBinding.inflate(inflater, container, false).apply {
            model = viewModel
            recyclerView.adapter = StatListAdapter(central.store, viewModel.pyDevice, central.dispatcher)
        }

        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.toolbar
            .setupWithNavController(findNavController(), appBarConfiguration)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelAutoRefresh()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStats()
    }
}

