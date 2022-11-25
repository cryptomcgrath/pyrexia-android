package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.util.DiffableItem

internal class ComponentDiffableItem(private val selectedFun: ComponentSelectedFun,
                                     private val component: Component) : DiffableItem {
    val imageResId = component.imageResId
    val nameResId = component.nameResId

    fun onClickItem() {
        selectedFun.invoke(component)
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is ComponentDiffableItem &&
                other.imageResId == imageResId &&
                other.nameResId == nameResId
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is ComponentDiffableItem
    }
}