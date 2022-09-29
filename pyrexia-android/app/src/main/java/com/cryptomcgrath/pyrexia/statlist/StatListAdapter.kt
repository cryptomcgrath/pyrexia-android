package com.cryptomcgrath.pyrexia.statlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.cryptomcgrath.pyrexia.BindFunViewHolder
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.cryptomcgrath.pyrexia.RxStoreAdapter
import com.cryptomcgrath.pyrexia.databinding.StatItemBinding
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore

internal class StatListAdapter(store: RxStore<StatListState>,
                               private val dispatcher: Dispatcher
): RxStoreAdapter<StatListState>(store) {
    override val viewTypes: List<Class<out DiffableItem>> =
        listOf(
            StatDiffableItem::class.java
        )

    override val differ: AsyncListDiffer<DiffableItem> =
        AsyncListDiffer(this, DIFF_CALLBACK)

    override fun buildList(state: StatListState): List<DiffableItem> {
        val items = mutableListOf<DiffableItem>()
        state.statList.forEach {
            items += StatDiffableItem(it, dispatcher)
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

            else -> throw IllegalStateException("unknown view type")
        }
    }
}