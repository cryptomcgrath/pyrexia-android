package com.cryptomcgrath.pyrexia

import androidx.lifecycle.ViewModel
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class ThermostatViewModel: ViewModel() {

    private val pyrexiaService = PyrexiaService()

    private val disposables = CompositeDisposable()

    init {
        refreshData()
    }

    private fun refreshData() {
        pyrexiaService.getProgramsList(BASE_URL)
            .subscribeBy(
                onSuccess = {

                },
                onError = {

                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

const val BASE_URL = "http://bigred.dyndns.net:8000/"
