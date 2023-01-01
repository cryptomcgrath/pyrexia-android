package com.cryptomcgrath.pyrexia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PyDevice(
    val uid: Int = 0,
    val name: String = "",
    val baseUrl: String = "",
    val email: String = "",
    val password: String = "",
    val token: String = ""
): Parcelable
