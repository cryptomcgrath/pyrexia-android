package com.cryptomcgrath.pyrexia.deviceconfig

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.util.DiffableItem


internal class SensorDiffableItem(val sensor: Sensor,
                                  val isEditMode: Boolean): DiffableItem {

    var name = sensor.name
    var addr = sensor.addr
    val nameError = ObservableField<String>()

    val sensorDrawableInt = sensor.sensorType?.imageResId ?: 0
    val addrHintResId = sensor.sensorType?.addrHintResId ?: R.string.sensor_addr_hint_generic

    fun onClickCancel(view: View) {

    }

    fun onClickOverflow(view: View) {

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