package com.cryptomcgrath.pyrexia.statlist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.cryptomcgrath.pyrexia.thermostat.AUTO_REFRESH_INTERVAL
import com.cryptomcgrath.pyrexia.thermostat.TAG
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

internal class StatListViewModel(): ViewModel() {
    val store = RxStore.create(statListReducerFun)
    val dispatcher = Dispatcher.create(store)
    internal val eventQueue = EventQueue.create()

    private val pyrexiaService = PyrexiaService()
    private val disposables = CompositeDisposable()

    init {
        relayEventsToFragment()
        refreshData()
        setupAutoRefresh()
    }

    private fun relayEventsToFragment() {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    eventQueue.post(it)
                },
                onError = {

                }
            ).addTo(disposables)
    }

    private fun refreshData() {
        pyrexiaService.getStatList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(StatListEvent.NewStatList(it))
                },
                onError = {
                    dispatcher.post(StatListEvent.ConnectionError(it))
                }
            ).addTo(disposables)
    }

    private fun setupAutoRefresh() {
        Observable.interval(AUTO_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    Log.d(TAG, "refreshing data")
                    refreshData()
                },
                onError = {
                    // ignore
                }
            ).addTo(disposables)
    }
}