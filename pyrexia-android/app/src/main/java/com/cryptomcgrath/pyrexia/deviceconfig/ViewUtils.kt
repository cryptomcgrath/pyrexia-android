package com.cryptomcgrath.pyrexia.deviceconfig

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.cryptomcgrath.pyrexia.R
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

private val lastUpdatedFormatter by lazy {
    SimpleDateFormat("MMM dd h:mma", Locale.US)
}

const val SECONDS_IN_DAY = 24*3600
const val SECONDS_IN_HOUR = 3600
const val SECONDS_IN_MINUTE = 60

internal fun Long.secsToLastUpdatedTimeString(): String {
    val elapsed = this
    val d = (elapsed / (SECONDS_IN_DAY)).toInt()
    val h = ((elapsed - d * SECONDS_IN_DAY) / SECONDS_IN_HOUR).toInt()
    val m = (elapsed - (d * SECONDS_IN_DAY) - (h * SECONDS_IN_HOUR)) / SECONDS_IN_MINUTE
    val s = elapsed - (d * SECONDS_IN_DAY) - (h * SECONDS_IN_HOUR) - (m * SECONDS_IN_MINUTE)

    return when {
        d > 0 -> lastUpdatedFormatter.format(this*1000)
        h > 1 -> "$h hours $m minutes ago"
        m == 1L -> "$m minute ago"
        m > 0 -> "$m minutes ago"
        else -> "$s seconds ago"
    }
}

internal fun Long.toLastUpdatedTimeString(): String {
    val now = Date().time / 1000
    val elapsed = now - this
    return elapsed.secsToLastUpdatedTimeString()
}

internal fun View.hideKeyboard() {
    val imm = ContextCompat.getSystemService(this.context, InputMethodManager::class.java)
    imm?.hideSoftInputFromWindow(this.windowToken, 0)
}

internal fun String.isPositiveInt(): Boolean {
    return when {
        this.toIntOrNull() == null -> false
        (this.toIntOrNull() ?: 0) < 0 -> false
        this.isEmpty() -> false
        else -> true
    }
}

internal fun String.isValidGpioPin(): Boolean {
    (this.toIntOrNull() ?: 0).let {
        return it in 1..40
    }
}

internal fun createNetworkErrorAlertDialog(context: Context,
                                           throwable: Throwable,
                                           buttonActionFun: () -> Unit): AlertDialog {
    return AlertDialog.Builder(context)
        .setPositiveButton(R.string.ok) { di, _ ->
            di.dismiss()
            buttonActionFun.invoke()
        }
        .setTitle(context.getString(R.string.network_error_title))
        .setMessage(throwable.toString())
        .create()
}

internal fun Throwable.toUserFriendlyMessageResId(): Int? {
    return when {
        this is SocketTimeoutException -> R.string.network_error_socket_timeout
        else -> null
    }
}