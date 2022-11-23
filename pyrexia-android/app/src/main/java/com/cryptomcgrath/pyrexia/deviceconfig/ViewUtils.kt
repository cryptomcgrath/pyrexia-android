package com.cryptomcgrath.pyrexia.deviceconfig

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.cryptomcgrath.pyrexia.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

private val lastUpdatedFormatter by lazy {
    SimpleDateFormat("MMM dd h:mma", Locale.US)
}

internal fun Long.toLastUpdatedTimeString(): String {
    val now = Date().time / 1000
    val elapsed = now - this
    val d = (elapsed / 24*60*60).toInt()
    val h = ((elapsed - d * 24*60*60) / 3600).toInt()
    val m = (elapsed - (d * 24*60*60) - (h * 3600)) / 60
    val s = elapsed - (d * 24*60*60) - (h * 3600) - (m * 60)

    return when {
        d > 0 -> lastUpdatedFormatter.format(this*1000)
        h > 0 -> "$h hours $m minutes ago"
        m > 0 -> "$m minutes ago"
        else -> "$s seconds ago"
    }
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