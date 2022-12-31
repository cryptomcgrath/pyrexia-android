package com.cryptomcgrath.pyrexia.deviceconfig

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.DiffableItemAdapter
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.databinding.ComponentItemBinding
import com.cryptomcgrath.pyrexia.databinding.FragmentAddComponentBinding
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal class AddComponentBottomSheetFragment: BottomSheetDialogFragment() {
    private val args: AddComponentBottomSheetFragmentArgs by navArgs()

    private val viewModel: DeviceConfigViewModel by viewModels(ownerProducer = {requireParentFragment()}) {
        DeviceConfigViewModel.Factory(
            application = requireActivity().application,
            pyDevice = args.pydevice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        FragmentAddComponentBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        ).apply {
            dialog.setContentView(root)
            recyclerView.adapter = ComponentAdapter {
                dismiss()
                viewModel.dispatcher.post(DeviceConfigEvent.OnComponentAddSelected(it))
            }
        }

    }
}

internal class ComponentAdapter(private val selectedFun: ComponentSelectedFun): DiffableItemAdapter() {
    override val viewTypes: List<Class<out DiffableItem>>
        get() = listOf()

    override val differ: AsyncListDiffer<DiffableItem> = AsyncListDiffer(this, DIFF_CALLBACK)

    init {
        differ.submitList(
            buildList()
        )
    }

    private fun buildList(): List<DiffableItem> {
        return Component.values().map {
            ComponentDiffableItem(selectedFun, it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindFunViewHolder {
        val binding = ComponentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BindFunViewHolder(binding) {
            binding.model = it as ComponentDiffableItem
        }
    }
}

internal typealias ComponentSelectedFun = (Component) -> Unit