package com.cryptomcgrath.pyrexia.service

internal data class GetProgramsRunDto(
    val message: String,
    val data: List<ProgramRunDto>
) {
    data class ProgramRunDto(
        val program_id: Int,
        val program_name: String,
        val sensor_id: Int,
        val sensor_name: String,
        val sensor_value: Float,
        val control_id: Int,
        val control_name: String,
        val mode: String,
        val enabled: Int,
        val set_point: Float,
        val last_on_time: Long,
        val last_off_time: Long,
        val min_run: Int
    )
}