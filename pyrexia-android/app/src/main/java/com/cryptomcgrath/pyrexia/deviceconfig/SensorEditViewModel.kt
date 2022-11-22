package com.cryptomcgrath.pyrexia.deviceconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Sensor
import java.text.SimpleDateFormat
import java.util.*

internal class SensorEditViewModel(sensor: Sensor): ViewModel() {

    class Factory(private val sensor: Sensor) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SensorEditViewModel(sensor) as T
        }
    }

    var name: String = sensor.name
    var addr: String = sensor.addr
    val addressHintResId = sensor.sensorType?.addrHintResId ?: R.string.sensor_addr_hint_generic
    var updateInterval: String = sensor.updateInterval.toString()
    val lastUpdated = sensor.lastUpdatedTs.toLastUpdatedTimeString()

    val sensorDrawableInt = sensor.sensorType?.imageResId ?: 0

}

private val lastUpdatedFormatter by lazy {
    SimpleDateFormat("MMM dd h:mma", Locale.US)
}

private fun Long.toLastUpdatedTimeString(): String {
    val now = Date().time / 1000
    val elapsed = now - this
    val d = (elapsed / 24*60*60).toInt()
    val h = ((elapsed - d * 24*60*60) / 3600).toInt()
    val m = (elapsed - (d * 24*60*60) - (h * 3600)) / 60
    val s = elapsed - (d * 24*60*60) - (h * 3600) - (m * 60)

    return when {
        d > 0 -> lastUpdatedFormatter.format(this*1000)
        h > 0 -> "$h hours $m minutes ago"
        m > 0 -> "$m minutes ago"
        else -> "$s seconds ago"
    }
}