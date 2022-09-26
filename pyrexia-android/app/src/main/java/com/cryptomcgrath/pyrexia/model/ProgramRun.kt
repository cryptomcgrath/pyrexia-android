package com.cryptomcgrath.pyrexia.model

internal data class ProgramRun(
    val program: Program,
    val sensor: Sensor,
    val control: Control
)