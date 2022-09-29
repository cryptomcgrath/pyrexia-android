package com.cryptomcgrath.pyrexia.statlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cryptomcgrath.pyrexia.databinding.FragmentStatListBinding
import com.cryptomcgrath.pyrexia.thermostat.ThermostatFragmentDirections

internal class StatListFragment: Fragment() {

    private val viewModel: StatListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is StatListEvent.OnStatSelected -> {
                    val action = ThermostatFragmentDirections.actionGlobalThermostatFragment2(event.id)
                    findNavController().navigate(action)
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
            recyclerView.adapter = StatListAdapter(viewModel.store, viewModel.dispatcher)
        }

        return binding.root
    }
}

