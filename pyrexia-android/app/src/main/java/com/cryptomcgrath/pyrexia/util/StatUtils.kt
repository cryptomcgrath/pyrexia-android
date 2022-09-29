package com.cryptomcgrath.pyrexia.util

internal fun Float.toFormattedTemperatureString(): String {
    return String.format("%3d°", this.toInt())
}