package com.cryptomcgrath.pyrexia.deviceconfig

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.databinding.FragmentSensorEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal class SensorEditBottomSheetFragment: BottomSheetDialogFragment() {
    private val args: SensorEditBottomSheetFragmentArgs by navArgs()
    private val viewModel: SensorEditViewModel by viewModels {
        SensorEditViewModel.Factory(args.pydevice, args.sensor)
    }
    private val deviceConfigViewModel: DeviceConfigViewModel by activityViewModels {
        DeviceConfigViewModel.Factory(args.pydevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)

        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                SensorEditEvent.SaveSensorSuccess -> {
                    deviceConfigViewModel.refreshData()
                    dismiss()
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        FragmentSensorEditBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        ).apply {
            model = viewModel
            dialog.setContentView(root)
        }
    }

}