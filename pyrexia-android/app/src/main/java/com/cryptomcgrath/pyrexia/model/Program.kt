package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Program(
    val id: Int = 0,
    val name: String = "",
    val setPoint: Float = 70f,
    val control_id: Int = 0,
    val sensor_id: Int = 0,
    val mode: Mode = Mode.HEAT,
    val enabled: Boolean = true
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
