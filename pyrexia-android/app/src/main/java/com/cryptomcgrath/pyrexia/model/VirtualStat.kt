package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VirtualStat(
    val program: Program = Program(),
    val sensor: Sensor = Sensor(),
    val control: Control = Control()
) : Parcelable