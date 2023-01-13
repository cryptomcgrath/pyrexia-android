package com.cryptomcgrath.pyrexia.deviceconfig

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.ControlItemBinding
import com.cryptomcgrath.pyrexia.databinding.DeviceConfigItemBinding
import com.cryptomcgrath.pyrexia.databinding.SensorItemBinding
import com.cryptomcgrath.pyrexia.databinding.VstatItemBinding
import com.cryptomcgrath.pyrexia.devicelist.PyDeviceDiffableItem
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore

internal class DeviceConfigAdapter(private val context: Context,
                                   store: RxStore<DeviceConfigState>,
                                   private val dispatcher: Dispatcher) : RxStoreAdapter<DeviceConfigState>(store) {
    override val viewTypes: List<Class<out DiffableItem>> =
        listOf(
            DeviceHeaderDiffableItem::class.java,
            SensorDiffableItem::class.java,
            ControlDiffableItem::class.java,
            VStatDiffableItem::class.java
        )

    override val differ: AsyncListDiffer<DiffableItem> =
        AsyncListDiffer(this, DIFF_CALLBACK)

    override fun buildList(state: DeviceConfigState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()
        state.pyDevice?.let {
            items += DeviceHeaderDiffableItem(dispatcher = dispatcher,
                pyDevice = state.pyDevice,
                isLoading = state.loading)

            state.stats.forEach {
                items += VStatDiffableItem(it, dispatcher)
            }

            state.sensors.forEach {
                items += SensorDiffableItem(
                    context = context,
                    dispatcher = dispatcher,
                    sensor = it)
            }

            state.controls.forEach {
                items += ControlDiffableItem(
                    dispatcher = dispatcher,
                    context = context,
                    isEditMode = false,
                    control = it
                )
            }
        }
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindFunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewTypes[viewType]) {
            DeviceHeaderDiffableItem::class.java -> {
                val binding = DeviceConfigItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as DeviceHeaderDiffableItem
                }
            }

            VStatDiffableItem::class.java -> {
                val binding = VstatItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as VStatDiffableItem
                }
            }

            SensorDiffableItem::class.java -> {
                val binding = SensorItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as SensorDiffableItem
                }
            }

            ControlDiffableItem::class.java -> {
                val binding = ControlItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as ControlDiffableItem
                }
            }

            else -> throw IllegalStateException("unknown view type")
        }
    }
}