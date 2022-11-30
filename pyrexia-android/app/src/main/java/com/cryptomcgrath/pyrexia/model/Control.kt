package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Control(
    val id: Int = 0,
    val name: String = "",
    val gpio: Int = 0,
    val gpioOnHigh: Boolean = false,
    val lastOnTime: Long = 0L,
    val lastOffTime: Long = 0L,
    val minRun: Int = 300,
    val minRest: Int = 300,
    val controlOn: Boolean = false,
    val numCycles: Int = 0,
    val totalRun: Int = 0,
    val runCapacity: Int = 0
): Parcelable