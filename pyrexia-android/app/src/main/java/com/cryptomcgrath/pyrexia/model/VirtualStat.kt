package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VirtualStat(
    val program: Program,
    val sensor: Sensor,
    val control: Control
) : Parcelable