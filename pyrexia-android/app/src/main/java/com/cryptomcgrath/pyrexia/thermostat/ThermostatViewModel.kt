package com.cryptomcgrath.pyrexia.thermostat

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.CentralState
import com.cryptomcgrath.pyrexia.DevicesRepo
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*

internal class ThermostatViewModel(
    private val repo: DevicesRepo,
    private val dispatcher: Dispatcher,
    private val store: RxStore<CentralState>,
    val pyDevice: PyDevice,
    val stat: VirtualStat): ViewModel() {

    class Factory(private val repo: DevicesRepo,
                  private val dispatcher: Dispatcher,
                  private val store: RxStore<CentralState>,
                  private val pyDevice: PyDevice,
                  private val stat: VirtualStat) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThermostatViewModel(repo, dispatcher, store, pyDevice, stat) as T
        }
    }
    internal val eventQueue = EventQueue.create()
    private val disposables = CompositeDisposable()
    private val statId = stat.program.id
    val backgroundColor = ObservableInt(R.color.black)
    val showError = ObservableBoolean(false)

    init {
        subscribeToStateChanges()

        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
            onNext = { event ->
                when (event) {
                    is ThermostatEvent.OnClickRefill -> refill(event.pyDevice, event.controlId)

                    is ThermostatEvent.RequestDisableStat -> disableStat(event.pyDevice, event.statId)

                    is ThermostatEvent.RequestEnableStat -> enableStat(event.pyDevice, event.statId)

                    is ThermostatEvent.RequestIncreaseTemp -> increaseTemp(event.pyDevice, event.statId)

                    is ThermostatEvent.RequestDecreaseTemp -> decreaseTemp(event.pyDevice, event.statId)

                    is ThermostatEvent.RequestHistoryBefore -> fetchMoreHistoryIfNeeded(event.pyDevice, event.statId, event.beforeTs.toLong())
                }
                eventQueue.post(event)
            },
            onError = {
                // ignore
            }
        ).addTo(disposables)
    }

    private fun subscribeToStateChanges() {
        store.stateStream.subscribeBy(
            onNext = {
                updateUi(it.getDeviceState(pyDevice))
            },
            onError = {
                // ignore
            }
        ).addTo(disposables)
    }

    private fun updateUi(deviceState: CentralState.DeviceState) {
        showError.set(
            deviceState.connectionError != null
        )
        deviceState.stats.firstOrNull { it.program.id == statId }?.let {
            backgroundColor.set(
                when {
                    !it.program.enabled -> R.color.grey42
                    it.control.controlOn && it.program.mode == Program.Mode.HEAT -> R.color.heating
                    it.control.controlOn && it.program.mode == Program.Mode.COOL -> R.color.cooling
                    else -> R.color.cobalt
                }
            )
        }
    }
    private fun fetchMoreHistoryIfNeeded(pyDevice: PyDevice, statId: Int, timeStamp: Long) {
        Single.create {
            val pages = store.state.getDeviceState(pyDevice).historyPages.filter {
                it.statId == statId && it.minTs != null && it.maxTs != null
            }.sortedBy {
                it.minTs
            }
            val isAlreadyInPage = pages.any {
                timeStamp >= (it.minTs ?: Int.MAX_VALUE) && timeStamp <= (it.maxTs ?: Int.MIN_VALUE)
            }
            val pageAbove = pages.filter {
                timeStamp < (it.minTs ?: 0)
            }.minByOrNull {
                (it.minTs ?: Int.MAX_VALUE) - timeStamp
            }

            val minPage = store.state.getDeviceState(pyDevice).getMinHistoryPageForStat(statId)
            //val isTsNeeded = timeStamp < (minPage?.minTs ?: Int.MAX_VALUE)
            val isTsNeeded = !isAlreadyInPage
            val isAllDataLoaded = minPage?.pageend == true
            //val requestBeforeTs = minPage?.minTs
            val requestBeforeTs = pageAbove?.minTs ?: timeStamp.toInt()
            it.onSuccess(Pair(requestBeforeTs ?: 0, !isAllDataLoaded && isTsNeeded))
        }.flatMapCompletable { (ts, needed) ->
            if (needed) {
                Log.d(TAG, "fetchMoreHistory timeStamp = ${ts}")
                repo.fetchHistory(
                    pyDevice = pyDevice,
                    statId = statId,
                    startTs = null,
                    endTs = ts
                ).toCompletable()
            } else {
                Completable.complete()
            }
        }.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // success
                },
                onError = {
                    // ignore
                }
            ).addTo(disposables)
    }

    fun onClickConnectionError() {
        //store.state.connectionError?.let {
        //    eventQueue.post(UiEvent.ServiceError(it))
       // }
    }

    private fun refill(pyDevice: PyDevice, controlId: Int) {
        repo.refill(pyDevice, controlId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // ignore
                },
                onError = {
                    eventQueue.post(ThermostatEvent.NetworkError(it, false))
                }
            ).addTo(disposables)
    }
    private fun disableStat(pyDevice: PyDevice, statId: Int) {
        repo.disableStat(pyDevice, statId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // ignore
                },
                onError = {
                    eventQueue.post(ThermostatEvent.NetworkError(it, false))
                }
            ).addTo(disposables)
    }

    private fun enableStat(pyDevice: PyDevice, statId: Int) {
        repo.enableStat(pyDevice, statId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // ignore
                },
                onError = {
                    eventQueue.post(ThermostatEvent.NetworkError(it, false))
                }
            ).addTo(disposables)
    }

    private fun increaseTemp(pyDevice: PyDevice, statId: Int) {
        repo.increaseTemp(pyDevice,statId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // ignore
                },
                onError = {
                    eventQueue.post(ThermostatEvent.NetworkError(it, false))
                }
            ).addTo(disposables)
    }

    private fun decreaseTemp(pyDevice: PyDevice, statId: Int) {
        repo.decreaseTemp(pyDevice,statId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // ignore
                },
                onError = {
                    eventQueue.post(ThermostatEvent.NetworkError(it, false))
                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

private const val TAG="ThermostatViewModel"

fun String.sentenceCase(): String {
    return this.lowercase(Locale.getDefault()).replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}
