package com.cryptomcgrath.pyrexia.deviceconfig

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher

internal class DeviceHeaderDiffableItem(
    private val dispatcher: Dispatcher,
    private val pyDevice: PyDevice,
    val isLoading: Boolean): DiffableItem {

    fun onClickOverflow(view: View?) {
        view?.hideKeyboard()
        view?.let { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.deviceheader_overflow, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.shutdown -> {
                    AlertDialog.Builder(view.context)
                        .setTitle(R.string.shutdown_title)
                        .setMessage(view.context.getString(R.string.shutdown_are_you_sure, pyDevice.name))
                        .setPositiveButton(R.string.yes) { _, _ -> dispatcher.post(DeviceConfigEvent.ShutdownDevice) }
                        .setNegativeButton(R.string.no) { _, _ -> }
                        .setIcon(R.drawable.ic_baseline_power_settings_new_24)
                        .show()
                    true
                }

                else -> false
            }
        }
        popupMenu.setForceShowIcon(true)
        popupMenu.show()
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is DeviceHeaderDiffableItem &&
                pyDevice == other.pyDevice &&
                isLoading == other.isLoading
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is DeviceHeaderDiffableItem
    }
}