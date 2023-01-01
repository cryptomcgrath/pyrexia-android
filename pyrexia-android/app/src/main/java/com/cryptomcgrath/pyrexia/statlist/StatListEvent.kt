package com.cryptomcgrath.pyrexia.statlist

import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.edwardmcgrath.blueflux.core.Event

internal sealed class StatListEvent: Event {

    data class NewStatList(val statList: List<VirtualStat>): StatListEvent()
    data class RefreshDataError(val throwable: Throwable): StatListEvent()
    data class NetworkError(val throwable: Throwable): StatListEvent()

    data class OnStatSelected(val id: Int, val name: String): StatListEvent()
    data class OnClickIncreaseTemp(val id: Int) : StatListEvent()
    data class OnClickDecreaseTemp(val id: Int) : StatListEvent()
    data class SetLoading(val isLoading: Boolean) : StatListEvent()
    object GoToLogin: StatListEvent()
}