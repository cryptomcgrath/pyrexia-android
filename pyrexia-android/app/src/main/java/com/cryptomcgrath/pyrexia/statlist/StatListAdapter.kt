package com.cryptomcgrath.pyrexia.statlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.CentralState
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.StatItemBinding
import com.cryptomcgrath.pyrexia.databinding.StatItemLoadingBinding
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore

internal class StatListAdapter(store: RxStore<CentralState>,
                               private val pyDevice: PyDevice,
                               private val dispatcher: Dispatcher
): RxStoreAdapter<CentralState>(store) {
    override val viewTypes: List<Class<out DiffableItem>> =
        listOf(
            StatDiffableItem::class.java,
            StatLoadingDiffableItem::class.java
        )

    override val differ: AsyncListDiffer<DiffableItem> =
        AsyncListDiffer(this, DIFF_CALLBACK)

    override fun buildList(state: CentralState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()

        state.getDeviceState(pyDevice).let { deviceState ->
            deviceState.stats.forEach {
                items += StatDiffableItem(it, pyDevice, dispatcher, false)
            }
            if (deviceState.stats.isEmpty() && deviceState.loading) {
                items += StatLoadingDiffableItem()
            }
        }

        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindFunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewTypes[viewType]) {
            StatDiffableItem::class.java -> {
                val binding = StatItemBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatDiffableItem
                }
            }

            StatLoadingDiffableItem::class.java -> {
                val binding = StatItemLoadingBinding.inflate(inflater, parent, false)
                BindFunViewHolder(binding) {
                    binding.model = it as StatLoadingDiffableItem
                }
            }

            else -> throw IllegalStateException("unknown view type")
        }
    }
}