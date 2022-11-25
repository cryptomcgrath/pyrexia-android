package com.cryptomcgrath.pyrexia.deviceconfig

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher


internal class SensorDiffableItem(private val context: Context,
                                  val dispatcher: Dispatcher,
                                  val sensor: Sensor): DiffableItem {

    val name = sensor.name
    val addr = if (sensor.sensorType == Sensor.SensorType.DHT22) {
        context.getString(R.string.sensor_gpio_text, sensor.addr.trim())
    } else {
        sensor.addr
    }
    val nameError = ObservableField<String>()

    val sensorDrawableInt = sensor.sensorType?.imageResId ?: 0

    fun onClickOverflow(view: View?) {
        view?.let { showPopupMenu(view) }
    }

    private fun showPopupMenu(view: View) {
        PopupMenu(view.context, view).apply {
            menuInflater.inflate(R.menu.deviceconfig_overflow, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.component_delete -> {
                        AlertDialog.Builder(view.context)
                            .setMessage(view.context.getString(R.string.component_delete_confirm))
                        .setPositiveButton(R.string.yes) { _, _ -> dispatcher.post(DeviceConfigEvent.GoToSensorDelete(sensor)) }
                            .setNegativeButton(R.string.no) { _, _ -> }
                            .show()
                        true
                    }

                    R.id.component_edit -> {
                        dispatcher.post(DeviceConfigEvent.GoToSensorEdit(sensor))
                        true
                    }

                    else -> false
                }
            }
            setForceShowIcon(true)
            show()
        }
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is SensorDiffableItem &&
                other.sensor == sensor
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is SensorDiffableItem
    }
}



@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}