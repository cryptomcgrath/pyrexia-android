package com.cryptomcgrath.pyrexia.model

import com.cryptomcgrath.pyrexia.service.AddStatDto
import com.cryptomcgrath.pyrexia.service.ControlUpdateDto
import com.cryptomcgrath.pyrexia.service.GetControlsDto
import com.cryptomcgrath.pyrexia.service.GetHistoryDto
import com.cryptomcgrath.pyrexia.service.GetSensorsDto
import com.cryptomcgrath.pyrexia.service.GetStatListDto
import com.cryptomcgrath.pyrexia.service.SensorUpdateDto
import com.cryptomcgrath.pyrexia.service.StatDto
import java.util.*

internal fun GetStatListDto.toStatList(): List<VirtualStat> {
    return this.data.map {
        it.toStat(this.current_time)
    }
}

fun StatDto.toStat(currentTime: Long? = null): VirtualStat {
    val program = Program(
        id = program_id,
        name = program_name,
        setPoint = set_point,
        enabled = enabled == 1,
        control_id = control_id,
        sensor_id = sensor_id,
        mode = Program.Mode.fromString(mode)
    )
    val control = Control(
        id = control_id,
        name = control_name,
        lastOnTime = last_on_time,
        lastOffTime = last_off_time,
        minRun = min_run,
        minRest = min_rest,
        gpio = gpio,
        gpioOnHigh = gpio_on_high == 1,
        controlOn = control_on == 1,
        totalRun = total_run,
        runCapacity = run_capacity
    )
    val sensor = Sensor(
        id = sensor_id,
        name = sensor_name,
        value = sensor_value,
        lastUpdatedTs = sensor_update_time
    )
    return VirtualStat(
        program = program,
        control = control,
        sensor = sensor,
        currentTimeSecs = current_time ?: currentTime
    )
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