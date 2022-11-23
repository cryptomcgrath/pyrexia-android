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
import com.cryptomcgrath.pyrexia.databinding.FragmentControlEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal class ControlEditBottomSheetFragment : BottomSheetDialogFragment() {
    private val args: ControlEditBottomSheetFragmentArgs by navArgs()

    private val viewModel: ControlEditViewModel by viewModels {
        ControlEditViewModel.Factory(
            application = requireActivity().application,
            pyDevice = args.pydevice,
            control = args.control
        )
    }
    private val deviceConfigViewModel: DeviceConfigViewModel by activityViewModels {
        DeviceConfigViewModel.Factory(args.pydevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                ControlEditEvent.SaveControlSuccess -> {
                    deviceConfigViewModel.refreshData()
                    dismiss()
                }

                is ControlEditEvent.ShowNetworkError -> {
                    createNetworkErrorAlertDialog(
                        context = requireContext(),
                        throwable = event.throwable
                    ) {
                        // do nothing on dismiss
                    }.show()
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        FragmentControlEditBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        ).apply {
            model = viewModel
            dialog.setContentView(root)
        }
    }
}