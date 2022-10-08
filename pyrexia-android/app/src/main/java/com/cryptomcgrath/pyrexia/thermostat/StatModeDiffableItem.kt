package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.util.DiffableItem

internal class StatModeDiffableItem(mode: Program.Mode) : DiffableItem {
    val modeText = mode.name.sentenceCase()

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is StatModeDiffableItem &&
                other.modeText == modeText
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is StatModeDiffableItem
    }
}