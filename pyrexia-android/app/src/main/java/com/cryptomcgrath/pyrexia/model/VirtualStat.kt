package com.cryptomcgrath.pyrexia.model

internal data class VirtualStat(
    val program: Program,
    val sensor: Sensor,
    val control: Control
)