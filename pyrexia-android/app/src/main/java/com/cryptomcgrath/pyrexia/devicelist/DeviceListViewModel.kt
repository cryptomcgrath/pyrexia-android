package com.cryptomcgrath.pyrexia.devicelist

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.CentralEvent
import com.cryptomcgrath.pyrexia.CentralStore
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class DeviceListViewModel(val central: CentralStore) : ViewModel() {
    class Factory(private val central: CentralStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceListViewModel(central) as T
        }
    }

    val eventQueue = EventQueue.create()
    internal val dispatcher get() = central.dispatcher
    private val disposables = CompositeDisposable()

    val showAddButton = ObservableBoolean()

    init {
        relayEventsToFragment()
    }

    private fun relayEventsToFragment() {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { event ->
                    updateUi()
                    eventQueue.post(event)
                },
                onError = {
                    // ignore
                }
            ).addTo(disposables)
    }

    private fun updateUi() {
        showAddButton.set(
            !central.state.hasAddEmptyItem()
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun onClickAdd() {
        if (!central.state.hasAddEmptyItem()) {
            dispatcher.post(CentralEvent.AddEmptyItem)
        }
    }
}