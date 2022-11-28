package com.cryptomcgrath.pyrexia.service

internal data class GetControlsDto(
    val message: String,
    val data: List<ControlDto>
) {
    data class ControlDto(
        val id: Int,
        val name: String,
        val gpio: Int,
        val gpio_on_hi: Int,
        val last_on_time: Long,
        val last_off_time: Long,
        val min_run: Int,
        val min_rest: Int,
        val control_on: Int
    )
}

internal data class ControlUpdateDto(
    val id: Int?,
    val name: String,
    val gpio: Int,
    val gpio_on_hi: Int,
    val min_run: Int,
    val min_rest: Int
)