package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class VirtualStat(
    val program: Program = Program(),
    val sensor: Sensor = Sensor(),
    val control: Control = Control(),
    val lastRefreshTimeSecs: Long = Date().time / 1000,
    val currentTimeSecs: Long? = null
) : Parcelable