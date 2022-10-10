package com.cryptomcgrath.pyrexia.service

internal class GetHistoryDto(
    val message: String,
    val data: List<HistoryDto>
) {
    data class HistoryDto(
        val id: Int,
        val program_id: Int,
        val set_point: Float,
        val action_ts: Long,
        val sensor_id: Int,
        val sensor_value: Float,
        val control_id: Int,
        val control_on: Int,
        val program_action: String,
        val control_action: String
    )
}