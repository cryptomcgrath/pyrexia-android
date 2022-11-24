package com.cryptomcgrath.pyrexia.deviceconfig

import android.content.Context
import android.view.View
import android.widget.ImageView
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
        dispatcher.post(DeviceConfigEvent.GoToSensorEdit(sensor))
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