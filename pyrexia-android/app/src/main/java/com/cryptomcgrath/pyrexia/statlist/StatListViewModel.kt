package com.cryptomcgrath.pyrexia.statlist

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.CentralStore
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class StatListViewModel(private val central: CentralStore,
                                 val pyDevice: PyDevice): ViewModel() {

    class Factory(private val central: CentralStore, private val pyDevice: PyDevice) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatListViewModel(central, pyDevice) as T
        }
    }

    internal val eventQueue = EventQueue.create()
    private val disposables = CompositeDisposable()

    val showError = ObservableBoolean(false)
    val errorText = ObservableField<String>()
    val loading = ObservableBoolean(false)

    init {
        relayEventsToFragment()
    }

    private fun relayEventsToFragment() {
        central.dispatcher.getEventBus()
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
        central.state.getDeviceState(pyDevice).let {
            //showError.set(store.state.connectionError != null)
            //store.state.connectionError?.let {
            //    errorText.set(it.message.orEmpty())
            //}
        }

    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

private const val TAG="StatListViewModel"