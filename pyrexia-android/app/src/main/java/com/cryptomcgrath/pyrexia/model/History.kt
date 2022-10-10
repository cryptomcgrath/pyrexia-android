package com.cryptomcgrath.pyrexia.model

internal data class History(
    val id: Int,
    val programId: Int,
    val setPoint: Float,
    val actionTs: Long,
    val sensorId: Int,
    val sensorValue: Float,
    val controlId: Int,
    val controlOn: Boolean,
    val programAction: Action,
    val controlAction: Action
) {
    enum class Action {
        COMMAND_ON,
        COMMAND_OFF,
        WAIT_SATISFIED,
        WAIT_CALL,
        WAIT_REST,
        WAIT_MIN_RUN,
        DISABLED;

        companion object {
            fun parse(name: String?): Action = values().firstOrNull {
                it.name.equals(name, ignoreCase=true)
            } ?: DISABLED
        }
    }
}