package com.cryptomcgrath.pyrexia

import SingletonHolder
import android.app.Application
import com.cryptomcgrath.pyrexia.db.PyrexiaDb
import com.cryptomcgrath.pyrexia.db.toDevice
import com.cryptomcgrath.pyrexia.db.toPyDeviceList
import com.cryptomcgrath.pyrexia.devicelist.DeviceListEvent
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.ReducerFun
import com.edwardmcgrath.blueflux.core.RxStore
import com.edwardmcgrath.blueflux.core.State
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class CentralStore private constructor(app: Application) {

    val store = RxStore.create(centralReducerFun)
    val dispatcher = Dispatcher.create(store)
    val stateStream get() = store.stateStream
    val state get() = store.state

    private val service: Map<Int, PyrexiaService> = hashMapOf()

    private val db = PyrexiaDb.getDatabase(app)
    private val disposables = CompositeDisposable()

    init {
        reactToDispatchedEvents()
        refreshDeviceList()
    }

    private fun reactToDispatchedEvents() {
        dispatcher.getEventBus()
            .subscribeBy(
                onNext = { event ->
                    when(event) {
                        is CentralEvent.AddDevice -> {
                            addDevice(event.pyDevice)
                        }
                        is CentralEvent.ForgetDevice -> {
                            forgetDevice(event.pyDevice)
                        }
                    }
                },
                onError = {
                    // ignore
                }
            ).addTo(disposables)
    }

    private fun refreshDeviceList() {
        getDeviceListSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(CentralEvent.NewDeviceList(it))
                },
                onError = {
                    dispatcher.post(CentralEvent.DatabaseError(it))
                }
            ).addTo(disposables)
    }

    private fun getDeviceListSingle(): Single<List<PyDevice>> {
        return db.devicesDao().devicesList()
            .map {
                it.toPyDeviceList()
            }
    }

    private fun forgetDevice(pyDevice: PyDevice) {
        removeDeviceCompletable(pyDevice)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    refreshDeviceList()
                },
                onError = {
                    dispatcher.post(CentralEvent.DatabaseError(it))
                }
            ).addTo(disposables)
    }

    private fun addDevice(pyDevice: PyDevice) {
        addDeviceCompletable(pyDevice)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    refreshDeviceList()
                },
                onError = {
                    dispatcher.post(CentralEvent.DatabaseError(it))
                }
            ).addTo(disposables)
    }

    private fun addDeviceCompletable(pyDevice: PyDevice): Completable {
        return db.devicesDao().addDevice(pyDevice.toDevice())
    }

    private fun removeDeviceCompletable(pyDevice: PyDevice): Completable {
        return db.devicesDao().deleteDevice(pyDevice.toDevice())
    }

    companion object : SingletonHolder<CentralStore, Application>(::CentralStore)
}

data class CentralState(
    val deviceList: List<PyDevice> = listOf(),
    val deviceMap: Map<Int, DeviceState> = hashMapOf(),
    val databaseError: Throwable? = null
): State {
    data class DeviceState(
        val sensors: List<Sensor> = emptyList(),
        val controls: List<Control> = emptyList(),
        val stats: List<VirtualStat> = emptyList(),
        val updating: Boolean = false
    )

    fun hasAddEmptyItem(): Boolean {
        return deviceList.any {
            it.name.isEmpty()
        }
    }
}

internal val centralReducerFun: ReducerFun<CentralState> = { inState, event ->
    val state = inState ?: CentralState()

    when (event) {
        is CentralEvent.NewDeviceList -> {
            val newMap = state.deviceMap.filter {
                it.key in event.deviceList.map { it.uid }
            }

            state.copy(
                deviceList = event.deviceList,
                deviceMap = newMap,
                databaseError = null
            )
        }

        CentralEvent.AddEmptyItem -> {
            val newList = state.deviceList.toMutableList()
            newList.add(PyDevice())
            state.copy(
                deviceList = newList
            )
        }

        CentralEvent.CancelEmptyItem -> {
            state.copy(
                deviceList = state.deviceList.filter {
                    it.uid != 0
                }
            )
        }

        is CentralEvent.DatabaseError -> {
            state.copy(
                databaseError = event.throwable
            )
        }

        else -> state
    }
}

internal sealed class CentralEvent : Event {
    data class NewDeviceList(val deviceList: List<PyDevice>) : CentralEvent()
    data class DatabaseError(val throwable: Throwable) : CentralEvent()
    data class AddDevice(val pyDevice: PyDevice) : CentralEvent()
    data class ForgetDevice(val pyDevice: PyDevice) : CentralEvent()
    object AddEmptyItem : CentralEvent()
    object CancelEmptyItem : CentralEvent()
}