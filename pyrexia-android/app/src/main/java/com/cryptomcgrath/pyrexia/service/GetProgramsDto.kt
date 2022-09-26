package com.cryptomcgrath.pyrexia.service

data class GetProgramsDto(
    val message: String,
    val data: List<ProgramDto>
) {
    data class ProgramDto(
        val id: Int,
        val name: String,
        val set_point: Float,
        val sensor_id: Int,
        val control_id: Int,
        val mode: String,
        val enabled: Int
    )
}