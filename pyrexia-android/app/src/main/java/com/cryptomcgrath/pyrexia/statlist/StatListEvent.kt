package com.cryptomcgrath.pyrexia.statlist

import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.edwardmcgrath.blueflux.core.Event

internal sealed class StatListEvent: Event {
    data class OnStatSelected(val stat: VirtualStat, val name: String): StatListEvent()
    data class ShowNetworkError(val throwable: Throwable, val finish: Boolean = false): StatListEvent()

}