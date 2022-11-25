package com.cryptomcgrath.pyrexia.service

internal data class GetSensorsDto(
    val message: String,
    val data: List<SensorDto>
) {
    data class SensorDto(
        val id: Int,
        val name: String,
        val sensor_type: String,
        val addr: String,
        val update_time: Long,
        val update_interval: Int,
        val value: Float
    )
}

internal data class SensorUpdateDto(
    val id: Int?,
    val name: String,
    val sensor_type: String,
    val addr: String,
    val update_interval: Int
)