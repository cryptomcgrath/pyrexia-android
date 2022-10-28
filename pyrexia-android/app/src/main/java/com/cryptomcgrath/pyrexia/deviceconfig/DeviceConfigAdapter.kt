package com.cryptomcgrath.pyrexia.deviceconfig

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.DeviceConfigItemBinding
import com.cryptomcgrath.pyrexia.databinding.SensorItemBinding
import com.cryptomcgrath.pyrexia.devicelist.PyDeviceDiffableItem
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore

internal class DeviceConfigAdapter(store: RxStore<DeviceConfigState>,
                                   private val dispatcher: Dispatcher) : RxStoreAdapter<DeviceConfigState>(store) {
    override val viewTypes: List<Class<out DiffableItem>> =
        listOf(
            PyDeviceDiffableItem::class.java,
            SensorDiffableItem::class.java
        )

    override val differ: AsyncListDiffer<DiffableItem> =
        AsyncListDiffer(this, DIFF_CALLBACK)

    override fun buildList(state: DeviceConfigState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()
        state.pyDevice?.let {
            items += PyDeviceDiffableItem(dispatcher = dispatcher,
                pyDevice = state.pyDevice,
                isEditMode = false)

            state.sensors.forEach {
                items += SensorDiffableItem(it, isEditMode = false)
            }
        }
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindFunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewTypes[viewType]) {
            PyDeviceDiffableItem::class.java -> {
                val binding = DeviceConfigItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as PyDeviceDiffableItem
                }
            }

            SensorDiffableItem::class.java -> {
                val binding = SensorItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as SensorDiffableItem
                }
            }

            else -> throw IllegalStateException("unknown view type")
        }
    }
}