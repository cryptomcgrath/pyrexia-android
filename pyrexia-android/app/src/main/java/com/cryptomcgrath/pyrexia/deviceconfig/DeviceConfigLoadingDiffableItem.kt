package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.util.DiffableItem

internal class DeviceConfigLoadingDiffableItem : DiffableItem {
    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is DeviceConfigLoadingDiffableItem
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is DeviceConfigLoadingDiffableItem
    }
}