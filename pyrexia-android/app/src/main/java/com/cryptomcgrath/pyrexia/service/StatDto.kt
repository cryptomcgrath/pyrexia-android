package com.cryptomcgrath.pyrexia.service

data class StatDto(
    val program_id: Int,
    val program_name: String,
    val sensor_id: Int,
    val sensor_name: String,
    val sensor_value: Float,
    val sensor_update_time: Long,
    val control_id: Int,
    val control_name: String,
    val mode: String,
    val enabled: Int,
    val set_point: Float,
    val last_on_time: Long,
    val last_off_time: Long,
    val min_run: Int,
    val min_rest: Int,
    val gpio: Int,
    val gpio_on_high: Int,
    val control_on: Int,
    val total_run: Int,
    val run_capacity: Int,
    val current_time: Long?
)

internal data class GetStatDto(
    val message: String,
    val data: StatDto
)