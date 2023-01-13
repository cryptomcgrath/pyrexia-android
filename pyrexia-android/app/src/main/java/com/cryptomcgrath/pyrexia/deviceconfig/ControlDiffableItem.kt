package com.cryptomcgrath.pyrexia.deviceconfig

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
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
    val gpioText = context.getString(R.string.control_gpio_text, control.gpio, control.gpioOnHigh.toHiLowText(context))
    val nameError = ObservableField<String>()

    fun onClickOverflow(view: View?) {
        view?.let { showPopupMenu(view) }
    }

    private fun showPopupMenu(view: View) {
        PopupMenu(view.context, view).apply {
            menuInflater.inflate(R.menu.deviceconfig_overflow, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.component_delete -> {
                        AlertDialog.Builder(view.context)
                            .setTitle(R.string.delete_dialog_title)
                            .setMessage(view.context.getString(R.string.component_delete_confirm, control.name))
                            .setPositiveButton(R.string.yes) { _, _ -> dispatcher.post(DeviceConfigEvent.GoToControlDelete(control)) }
                            .setNegativeButton(R.string.no) { _, _ -> }
                            .setIcon(R.drawable.ic_outline_delete_24)
                            .show()
                        true
                    }

                    R.id.component_edit -> {
                        dispatcher.post(DeviceConfigEvent.GoToControlEdit(control))
                        true
                    }

                    else -> false
                }
            }
            setForceShowIcon(true)
            show()
        }
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