package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
): Parcelable