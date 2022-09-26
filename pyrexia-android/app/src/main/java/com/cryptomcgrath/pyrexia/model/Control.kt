package com.cryptomcgrath.pyrexia.model

data class Control(
    val id: Int,
    val name: String,
    val running: Boolean,
    val lastOnTime: Long,
    val lastOffTime: Long,
    val minRun: Int
)