package com.cryptomcgrath.pyrexia.devicelist


import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.ObservableField
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher


class PyDeviceDiffableItem(private val dispatcher: Dispatcher,
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
        if (view != null) {
            val imm = getSystemService(view.context, InputMethodManager::class.java)
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
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

    fun onClickCancel() {
        dispatcher.post(DeviceListEvent.CancelEmptyItem)
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