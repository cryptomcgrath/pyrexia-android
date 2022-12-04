package com.cryptomcgrath.pyrexia.deviceconfig

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.databinding.FragmentStatEditBinding
import com.cryptomcgrath.pyrexia.model.Program
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal class StatEditBottomSheetFragment: BottomSheetDialogFragment() {
    private val args: StatEditBottomSheetFragmentArgs by navArgs()
    private val viewModel: StatEditViewModel by viewModels {
        StatEditViewModel.Factory(
            application = requireActivity().application,
            pyDevice = args.pydevice,
            stat = args.stat)
    }
    private val deviceConfigViewModel: DeviceConfigViewModel by activityViewModels {
        DeviceConfigViewModel.Factory(
            application = requireActivity().application,
            pyDevice = args.pydevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)

        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                StatEditViewModel.StatEditUiEvent.SaveStatSuccess -> {
                    deviceConfigViewModel.refreshData()
                    dismiss()
                }

                is StatEditViewModel.StatEditUiEvent.ShowNetworkError -> {
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
        FragmentStatEditBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        ).apply {
            model = viewModel
            val adapter = NoFilterAdapter(
                requireContext(),
                R.layout.mode_dropdown_item,
                Program.Mode.values().map { it.slug }
            )
            modeAutoComplete.setAdapter(adapter)
            dialog.setContentView(root)
        }
    }
}

class NoFilterAdapter<T>(context: Context, layout: Int, var values: List<T>) :
    ArrayAdapter<T>(context, layout, values) {
    private val filterNothing = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            results.values = values
            results.count = values.size
            return results
        }
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterNothing
    }
}
