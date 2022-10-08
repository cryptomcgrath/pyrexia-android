package com.cryptomcgrath.pyrexia.thermostat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.StatEnabledItemBinding
import com.cryptomcgrath.pyrexia.databinding.StatItemBinding
import com.cryptomcgrath.pyrexia.databinding.StatModeItemBinding
import com.cryptomcgrath.pyrexia.databinding.ThermostatItemBinding
import com.cryptomcgrath.pyrexia.databinding.ThermostatItemLoadingBinding
import com.cryptomcgrath.pyrexia.statlist.StatDiffableItem
import com.cryptomcgrath.pyrexia.statlist.StatLoadingDiffableItem
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore

internal class ThermostatAdapter(store: RxStore<ThermostatState>,
                                 private val dispatcher: Dispatcher
): RxStoreAdapter<ThermostatState>(store) {
    override val viewTypes: List<Class<out DiffableItem>> =
        listOf(
            StatDiffableItem::class.java,
            StatModeDiffableItem::class.java,
            StatEnabledDiffableItem::class.java,
            StatLoadingDiffableItem::class.java
        )

    override val differ: AsyncListDiffer<DiffableItem> = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun buildList(state: ThermostatState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()
        if (state.isLoading) {
            items += StatLoadingDiffableItem()
        } else {
            state.current?.let {
                items += StatDiffableItem(state.current, dispatcher)
                items += StatModeDiffableItem(state.current.program.mode)
                items += StatEnabledDiffableItem(dispatcher, state.current.program.enabled, state.current.program.id)
            }
        }
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindFunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewTypes[viewType]) {
            StatDiffableItem::class.java -> {
                val binding = ThermostatItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatDiffableItem
                }
            }

            StatModeDiffableItem::class.java -> {
                val binding = StatModeItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatModeDiffableItem
                }
            }

            StatEnabledDiffableItem::class.java -> {
                val binding = StatEnabledItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatEnabledDiffableItem
                }
            }

            StatLoadingDiffableItem::class.java -> {
                val binding = ThermostatItemLoadingBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatLoadingDiffableItem
                }
            }

            else -> throw IllegalStateException("unknown view type")
        }
    }
}