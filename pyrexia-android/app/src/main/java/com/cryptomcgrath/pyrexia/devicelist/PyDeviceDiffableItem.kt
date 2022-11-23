package com.cryptomcgrath.pyrexia.devicelist


import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.ObservableField
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.deviceconfig.hideKeyboard
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher


internal class PyDeviceDiffableItem(private val dispatcher: Dispatcher,
                                    private val pyDevice: PyDevice,
                                    val isEditMode: Boolean) : DiffableItem {
    var name = pyDevice.name
    var url = pyDevice.baseUrl

    val nameError = ObservableField<String>()
    val urlError = ObservableField<String>()

    fun onClickImage() {
        if (!isEditMode) {
            dispatcher.post(DeviceListEvent.GoToStatList(pyDevice))
        }
    }

    fun onEditorAction(view: TextView?, @Suppress("UNUSED_PARAMETER") actionId: Int?, event: KeyEvent?): Boolean {
        view?.hideKeyboard()

        if (event?.action == KeyEvent.ACTION_DOWN || actionId == EditorInfo.IME_ACTION_DONE) {
            if (!checkErrors()) {
                dispatcher.post(
                    DeviceListEvent.AddDevice(PyDevice(name = name, baseUrl = url))
                )
                return true
            }
        }
        return false
    }

    private fun checkErrors(): Boolean {
        nameError.set(null)
        urlError.set(null)

        var error = false

        if (name.isEmpty()) {
            nameError.set("Name cannot be blank")
            error  = true
        }
        if (url.isEmpty()) {
            urlError.set("Url cannot be blank")
            error = true
        } else if (!URLUtil.isValidUrl(url)) {
            urlError.set("Invalid Url")
            error = true
        }
        return error
    }

    fun onClickCancel(view: View?) {
        view?.hideKeyboard()
        dispatcher.post(DeviceListEvent.CancelEmptyItem)
    }

    fun onClickOverflow(view: View?) {
        view?.hideKeyboard()
        view?.let { showPopupMenu(it) }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.pydevice_overflow, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.forget -> {
                    AlertDialog.Builder(view.context)
                        .setMessage(view.context.getString(R.string.forget_are_you_sure, pyDevice.name))
                        .setPositiveButton(R.string.yes) { _, _ -> dispatcher.post(DeviceListEvent.ForgetDevice(pyDevice)) }
                        .setNegativeButton(R.string.no) { _, _ -> }
                        .show()
                    true
                }

                R.id.configure -> {
                    dispatcher.post(DeviceListEvent.GoToDeviceConfig(pyDevice))
                    true
                }

                else -> false
            }
        }
        popupMenu.setForceShowIcon(true)
        popupMenu.show()
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is PyDeviceDiffableItem &&
                other.name == name &&
                other.url == url
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is PyDeviceDiffableItem &&
                other.pyDevice.uid == pyDevice.uid
    }
}

