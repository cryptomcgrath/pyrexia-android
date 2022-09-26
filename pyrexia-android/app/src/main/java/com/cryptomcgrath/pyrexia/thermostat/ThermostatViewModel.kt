package com.cryptomcgrath.pyrexia.thermostat

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class ThermostatViewModel: ViewModel() {
    private val store = RxStore.create(thermostatReducerFun)
    private val dispatcher = Dispatcher.create(store)
    internal val eventQueue = EventQueue.create()

    private val pyrexiaService = PyrexiaService()
    private val disposables = CompositeDisposable()

    val name = ObservableField<String>()

    init {
        refreshData()
        subscribeToStateChanges()
    }

    private fun subscribeToStateChanges() {
        store.stateStream.subscribeBy(
            onNext = {
                updateUi(it)
            },
            onError = {
                // ignore
            }
        ).addTo(disposables)
    }

    private fun updateUi(state: ThermostatState) {
        state.program?.let {
            name.set(it.program.name)
        }
    }

    private fun refreshData() {
        pyrexiaService.getProgramsRunList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(ThermostatEvent.NewProgramsRun(it))
                },
                onError = {
                    eventQueue.post(UiEvent.ServiceError(it))
                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    sealed class UiEvent: Event {
        data class ServiceError(val throwable: Throwable): UiEvent()
    }
}

const val BASE_URL = "http://bigred.ddns.net:8000/"
