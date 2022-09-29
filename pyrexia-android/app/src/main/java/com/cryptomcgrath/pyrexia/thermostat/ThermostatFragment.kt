package com.cryptomcgrath.pyrexia.thermostat

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.databinding.FragmentThermostatBinding


internal class ThermostatFragment: Fragment() {
    private val args: ThermostatFragmentArgs by navArgs()

    private val viewModel: ThermostatViewModel by viewModels {
        ThermostatViewModel.Factory(args.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is ThermostatViewModel.UiEvent.ServiceError -> {
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
        return FragmentThermostatBinding.inflate(inflater, container, false).apply {
            model = viewModel
        }.root
    }

    override fun onResume() {
        super.onResume()
        /*(requireActivity() as? AppCompatActivity)?.actionBar?.let { actionBar ->
            actionBar.setTitle("First Fragment")
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }*/

    }

    private fun showServicesError(throwable: Throwable) {
        AlertDialog.Builder(requireActivity())
            .setPositiveButton(R.string.ok, null)
            .setTitle("Service Error")
            .setMessage(throwable.toString())
            .create().show()
    }
}