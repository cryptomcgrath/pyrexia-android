package com.cryptomcgrath.pyrexia.model

data class Control(
    val id: Int,
    val name: String,
    val gpio: Int,
    val gpioOnHigh: Boolean,
    val lastOnTime: Long,
    val lastOffTime: Long,
    val minRun: Int,
    val minRest: Int,
    val controlOn: Boolean
)