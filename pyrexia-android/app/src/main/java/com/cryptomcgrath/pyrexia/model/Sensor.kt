package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import com.cryptomcgrath.pyrexia.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sensor(
    val id: Int,
    val name: String,
    val value: Float,
    val sensorType: SensorType? = null,
    val addr: String = "",
    val updateInterval: Int = 0,
    val lastUpdatedTs: Long = 0L
): Parcelable {
    enum class SensorType(val imageResId: Int, val addrHintResId: Int) {
        DHT22(R.drawable.dht22_snip2, R.string.sensor_addr_hint_dht22),
        SENSORPUSH(R.drawable.sp_snip, R.string.sensor_addr_hint_sp);
    }
}