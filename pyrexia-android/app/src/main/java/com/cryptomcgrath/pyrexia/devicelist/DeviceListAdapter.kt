package com.cryptomcgrath.pyrexia.devicelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.DeviceItemBinding
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore

internal class DeviceListAdapter(store: RxStore<DeviceListState>,
                        private val dispatcher: Dispatcher) : RxStoreAdapter<DeviceListState>(store) {
    override val viewTypes: List<Class<out DiffableItem>> =
        listOf(
            PyDeviceDiffableItem::class.java
        )

    override val differ: AsyncListDiffer<DiffableItem> =
        AsyncListDiffer(this, DIFF_CALLBACK)

    override fun buildList(state: DeviceListState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()

        state.deviceList.forEach {
            items += PyDeviceDiffableItem(
                dispatcher = dispatcher,
                pyDevice = it,
                isEditMode = it.name.isEmpty())
        }
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindFunViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return when (viewTypes[viewType]) {
            PyDeviceDiffableItem::class.java -> {
                val binding = DeviceItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as PyDeviceDiffableItem
                }
            }

            else -> throw IllegalStateException("unknown view type")
        }
    }
}