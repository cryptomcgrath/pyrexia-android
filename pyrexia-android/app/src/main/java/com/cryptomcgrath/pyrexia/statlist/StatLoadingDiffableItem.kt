package com.cryptomcgrath.pyrexia.statlist

import com.cryptomcgrath.pyrexia.util.DiffableItem

internal class StatLoadingDiffableItem : DiffableItem {
    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is StatLoadingDiffableItem
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is StatLoadingDiffableItem
    }
}