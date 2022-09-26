package com.cryptomcgrath.pyrexia.model

import com.cryptomcgrath.pyrexia.service.GetProgramsDto

fun GetProgramsDto.toProgramList(): List<Program> {
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