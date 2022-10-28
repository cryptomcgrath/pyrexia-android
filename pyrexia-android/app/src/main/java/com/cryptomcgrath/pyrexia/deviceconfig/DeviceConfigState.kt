package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.edwardmcgrath.blueflux.core.State

internal data class DeviceConfigState(
    val pyDevice: PyDevice? = null,
    val sensors: List<Sensor> = emptyList()
): State