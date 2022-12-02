package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale

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
    enum class Mode {
        HEAT,
        COOL;

        companion object {
            fun fromString(s: String?): Mode {
                return when(s?.uppercase(Locale.US)) {
                    "heat" -> Mode.HEAT
                    "cool" -> Mode.COOL
                    else -> Mode.HEAT
                }
            }
        }

    }
}
