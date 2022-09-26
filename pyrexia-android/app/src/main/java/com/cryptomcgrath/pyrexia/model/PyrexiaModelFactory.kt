package com.cryptomcgrath.pyrexia.model

import com.cryptomcgrath.pyrexia.service.GetProgramsDto
import com.cryptomcgrath.pyrexia.service.GetProgramsRunDto

internal fun GetProgramsDto.toProgramList(): List<Program> {
    return this.data.map {
        Program(
            id = it.id,
            name = it.name,
            sensor_id = it.sensor_id,
            control_id = it.control_id,
            mode = Program.Mode.fromString(it.mode),
            enabled = it.enabled == 1,
            setPoint = it.set_point
        )
    }
}

internal fun GetProgramsRunDto.toProgramRunList(): List<ProgramRun> {
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
            running = false,
            lastOnTime = it.last_on_time,
            lastOffTime = it.last_off_time,
            minRun = it.min_run
        )
        val sensor = Sensor(
            id = it.sensor_id,
            name = it.sensor_name,
            value = it.sensor_value
        )
        ProgramRun(
            program = program,
            control = control,
            sensor = sensor
        )
    }
}