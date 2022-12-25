package com.cryptomcgrath.pyrexia.model

import com.cryptomcgrath.pyrexia.service.AddStatDto
import com.cryptomcgrath.pyrexia.service.ControlUpdateDto
import com.cryptomcgrath.pyrexia.service.GetControlsDto
import com.cryptomcgrath.pyrexia.service.GetHistoryDto
import com.cryptomcgrath.pyrexia.service.GetSensorsDto
import com.cryptomcgrath.pyrexia.service.GetStatListDto
import com.cryptomcgrath.pyrexia.service.SensorUpdateDto
import java.util.*

internal fun GetStatListDto.toStatList(): List<VirtualStat> {
    return this.data.map {
        val program = Program(
            id = it.program_id,
            name = it.program_name,
            setPoint = it.set_point,
            enabled = it.enabled == 1,
            control_id = it.control_id,
            sensor_id = it.sensor_id,
            mode = Program.Mode.fromString(it.mode)
        )
        val control = Control(
            id = it.control_id,
            name = it.control_name,
            lastOnTime = it.last_on_time,
            lastOffTime = it.last_off_time,
            minRun = it.min_run,
            minRest = it.min_rest,
            gpio = it.gpio,
            gpioOnHigh = it.gpio_on_high == 1,
            controlOn = it.control_on == 1,
            totalRun = it.total_run,
            runCapacity = it.run_capacity
        )
        val sensor = Sensor(
            id = it.sensor_id,
            name = it.sensor_name,
            value = it.sensor_value,
            lastUpdatedTs = it.sensor_update_time
        )
        VirtualStat(
            program = program,
            control = control,
            sensor = sensor,
            currentTimeSecs = current_time
        )
    }
}

internal fun GetHistoryDto.toHistoryList(): List<History> {
    return this.data.map {
        History(
            id = it.id,
            programId = it.program_id,
            setPoint = it.set_point,
            actionTs = it.action_ts,
            sensorId = it.sensor_id,
            sensorValue = it.sensor_value,
            controlId = it.control_id,
            controlOn = it.control_on == 1,
            programAction = History.Action.parse(it.program_action),
            controlAction = History.Action.parse(it.control_action)
        )
    }
}

internal fun GetSensorsDto.toSensorList(): List<Sensor> {
    return this.data.map {
        Sensor(
            id = it.id,
            name = it.name,
            value = it.value,
            sensorType = it.sensor_type.toSensorType(),
            updateInterval = it.update_interval,
            lastUpdatedTs = it.update_time,
            addr = it.addr
        )
    }
}

private fun String?.toSensorType(): Sensor.SensorType? {
    return when (this?.uppercase(Locale.US)) {
        "SP" -> Sensor.SensorType.SENSORPUSH
        "DHT22" -> Sensor.SensorType.DHT22
        else -> null
    }
}

internal fun GetControlsDto.toControlsList(): List<Control> {
    return this.data.map {
        Control(
            id = it.id,
            name = it.name,
            gpio = it.gpio,
            gpioOnHigh = it.gpio_on_hi == 1,
            minRest = it.min_rest,
            minRun = it.min_run,
            controlOn = it.control_on == 1,
            lastOnTime = it.last_on_time,
            lastOffTime = it.last_off_time,
            runCapacity = it.run_capacity,
            totalRun = it.total_run
        )
    }
}

internal fun Sensor.toSensorUpdateDto(): SensorUpdateDto {
    return SensorUpdateDto(
        id = if (this.id == 0) null else this.id,
        name = this.name,
        sensor_type = this.sensorType.toSensorTypeDtoString(),
        addr = this.addr,
        update_interval = this.updateInterval
    )
}

private fun Sensor.SensorType?.toSensorTypeDtoString(): String {
    return when(this) {
        Sensor.SensorType.SENSORPUSH -> "sp"
        Sensor.SensorType.DHT22 -> "dht22"
        else -> ""
    }
}

internal fun Control.toControlUpdateDto(): ControlUpdateDto {
    return ControlUpdateDto(
        id = if (this.id == 0) null else this.id,
        name = this.name,
        gpio = this.gpio,
        gpio_on_hi = if (gpioOnHigh) 1 else 0,
        min_run = this.minRun,
        min_rest = this.minRest,
        run_capacity = this.runCapacity
    )
}

internal fun Program.toAddStatDto(): AddStatDto {
    return AddStatDto(
        name = this.name,
        mode = this.mode.name,
        enabled = if (this.enabled) 1 else 0,
        sensor_id = this.sensor_id,
        control_id = this.control_id,
        set_point = this.setPoint
    )
}