package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.R

internal enum class Component(
    val imageResId: Int,
    val nameResId: Int
) {
    VSTAT(R.drawable.vstat_snip, R.string.component_vstat),
    RELAY(R.drawable.relay_snip1, R.string.component_relay),
    DHT22(R.drawable.dht22_snip2, R.string.component_sensor_dht22),
    SENSORPUSH(R.drawable.sp_snip, R.string.component_sensor_sp);
}