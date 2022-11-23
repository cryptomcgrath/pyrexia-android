package com.cryptomcgrath.pyrexia.deviceconfig

import android.content.Context
import android.view.View
import androidx.databinding.ObservableField
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher

internal class ControlDiffableItem(context: Context,
                                   private val dispatcher: Dispatcher,
                                   val control: Control,
                                   val isEditMode: Boolean): DiffableItem {
    var name = control.name
    val gpioText = context.getString(R.string.gpio_text, control.gpio, control.gpioOnHigh.toHiLowText(context))
    val nameError = ObservableField<String>()

    fun onClickOverflow(view: View?) {
        dispatcher.post(DeviceConfigEvent.GoToControlEdit(control))
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is ControlDiffableItem &&
                other.control == control
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is ControlDiffableItem
    }
}

private fun Boolean.toHiLowText(context: Context): String {
    return if (this) context.getString(R.string.high) else context.getString(R.string.low)
}