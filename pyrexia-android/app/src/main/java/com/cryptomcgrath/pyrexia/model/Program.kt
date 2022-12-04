package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Program(
    val id: Int,
    val name: String,
    val setPoint: Float,
    val control_id: Int,
    val sensor_id: Int,
    val mode: Mode,
    val enabled: Boolean
): Parcelable {
    enum class Mode(val slug: String) {
        HEAT("heat"),
        COOL("cool");

        companion object {
            fun fromString(s: String?): Mode {
                return values().firstOrNull {
                    it.slug.equals(s, ignoreCase = true)
                } ?: HEAT
            }
        }

    }
}
