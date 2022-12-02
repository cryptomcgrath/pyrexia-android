package com.cryptomcgrath.pyrexia.service

internal data class AddStatDto(
    val name: String,
    val mode: String,
    val enabled: Int,
    val sensor_id: Int,
    val set_point: Float,
    val control_id: Int
)