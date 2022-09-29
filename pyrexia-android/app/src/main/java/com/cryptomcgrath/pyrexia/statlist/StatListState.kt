package com.cryptomcgrath.pyrexia.statlist

import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.State

internal data class StatListState(
    val statList: List<ProgramRun> = emptyList(),
    val connectionError: Throwable? = null
): State