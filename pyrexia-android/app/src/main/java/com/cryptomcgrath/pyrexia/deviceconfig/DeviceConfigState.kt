package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.edwardmcgrath.blueflux.core.State

internal data class DeviceConfigState(
    val pyDevice: PyDevice? = null,
    val sensors: List<Sensor> = emptyList(),
    val controls: List<Control> = emptyList(),
    val stats: List<ProgramRun> = emptyList()
): State