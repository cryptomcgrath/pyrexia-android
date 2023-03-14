package com.cryptomcgrath.pyrexia.statlist

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.AUTO_REFRESH_INTERVAL
import com.cryptomcgrath.pyrexia.CentralEvent
import com.cryptomcgrath.pyrexia.DevicesRepo
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.service.isUnauthorized
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

internal class StatListViewModel(private val repo: DevicesRepo,
                                 private val dispatcher: Dispatcher,
                                 val pyDevice: PyDevice): ViewModel() {

    class Factory(private val repo: DevicesRepo, private val dispatcher: Dispatcher, private val pyDevice: PyDevice) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatListViewModel(repo, dispatcher, pyDevice) as T
        }
    }

    internal val eventQueue = EventQueue.create()
    private val disposables = CompositeDisposable()
    private var autoRefreshDisposable: Disposable? = null

    val showError = ObservableBoolean(false)
    val errorText = ObservableField<String>()
    val loading = ObservableBoolean(false)

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
                    Log.e(TAG, "error relaying event to fragment ${it.message}")
                }
            ).addTo(disposables)
    }

    private fun updateUi() {
       // repo.state.getDeviceState(pyDevice).let {
            //showError.set(store.state.connectionError != null)
            //store.state.connectionError?.let {
            //    errorText.set(it.message.orEmpty())
            //}
       // }
    }

    fun refreshStats() {
        repo.refreshStats(pyDevice)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    if (autoRefreshDisposable == null) {
                        setupAutoRefresh()
                    }
                },
                onError = {
                    if (it.isUnauthorized()) {
                        dispatcher.post(CentralEvent.GoToLogin(pyDevice))
                    } else {
                        eventQueue.post(StatListEvent.ShowNetworkError(it, true))
                    }
                }
            ).addTo(disposables)
    }

    private fun setupAutoRefresh() {
        autoRefreshDisposable = Observable.interval(AUTO_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    refreshStats()
                },
                onError = {
                    // ignore
                }
            )
    }
    fun cancelAutoRefresh() {
        autoRefreshDisposable?.dispose()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        autoRefreshDisposable?.dispose()
    }
}

private const val TAG="StatListViewModel"