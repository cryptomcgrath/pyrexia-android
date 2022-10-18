package com.cryptomcgrath.pyrexia.statlist

import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.Event

internal sealed class StatListEvent: Event {

    data class NewStatList(val statList: List<ProgramRun>): StatListEvent()
    data class ConnectionError(val throwable: Throwable): StatListEvent()

    data class OnStatSelected(val id: Int, val name: String): StatListEvent()
    data class OnClickIncreaseTemp(val id: Int) : StatListEvent()
    data class OnClickDecreaseTemp(val id: Int) : StatListEvent()
    data class SetLoading(val isLoading: Boolean) : StatListEvent()
}