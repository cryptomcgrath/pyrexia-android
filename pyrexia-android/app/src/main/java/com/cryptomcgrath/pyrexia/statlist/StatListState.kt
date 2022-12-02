package com.cryptomcgrath.pyrexia.statlist

import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.edwardmcgrath.blueflux.core.State

internal data class StatListState(
    val isLoading: Boolean = false,
    val dataLoaded: Boolean = false,
    val statList: List<VirtualStat> = emptyList(),
    val connectionError: Throwable? = null
): State