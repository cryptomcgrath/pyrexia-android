package com.cryptomcgrath.pyrexia.statlist

import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.Event

internal sealed class StatListEvent: Event {

    data class NewStatList(val statList: List<ProgramRun>): StatListEvent()
    data class ConnectionError(val throwable: Throwable): StatListEvent()

    data class OnStatSelected(val id: Int): StatListEvent()

}