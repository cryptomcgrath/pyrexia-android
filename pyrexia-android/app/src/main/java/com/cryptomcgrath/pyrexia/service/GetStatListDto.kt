package com.cryptomcgrath.pyrexia.service

internal data class GetStatListDto(
    val message: String,
    val data: List<StatDto>,
    val current_time: Long
)