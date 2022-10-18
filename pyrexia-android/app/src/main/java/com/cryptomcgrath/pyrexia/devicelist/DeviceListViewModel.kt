package com.cryptomcgrath.pyrexia.devicelist

import android.app.Application
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import com.cryptomcgrath.pyrexia.db.PyrexiaDb
import com.cryptomcgrath.pyrexia.db.toDevice
import com.cryptomcgrath.pyrexia.db.toPyDeviceList
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.statlist.StatListEvent
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

internal class DeviceListViewModel(application: Application) : AndroidViewModel(application) {
    val store = RxStore.create(deviceListReducerFun)
    val dispatcher = Dispatcher.create(store)
    val eventQueue = EventQueue.create()

    private val db = PyrexiaDb.getDatabase(application)
    private val disposables = CompositeDisposable()

    val showAddButton = ObservableBoolean()

    init {
        relayEventsToFragment()
        refreshData()
    }

    private fun relayEventsToFragment() {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { event ->
                    when (event) {
                        is DeviceListEvent.AddDevice -> {
                            addDevice(event.pyDevice)
                        }
                        is DeviceListEvent.ForgetDevice -> {
                            forgetDevice(event.pyDevice)
                        }
                    }
                    updateUi()
                    eventQueue.post(event)
                },
                onError = {

                }
            ).addTo(disposables)
    }

    private fun updateUi() {
        showAddButton.set(
            !store.state.hasAddEmptyItem()
        )
    }

    private fun refreshData() {
        getDeviceListSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(DeviceListEvent.NewDeviceList(it))
                },
                onError = {
                    dispatcher.post(DeviceListEvent.DatabaseError(it))
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
                    refreshData()
                },
                onError = {
                    dispatcher.post(DeviceListEvent.DatabaseError(it))
                }
            ).addTo(disposables)
    }

    private fun addDevice(pyDevice: PyDevice) {
        addDeviceCompletable(pyDevice)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    refreshData()
                },
                onError = {
                    dispatcher.post(DeviceListEvent.DatabaseError(it))
                }
            ).addTo(disposables)
    }

    private fun addDeviceCompletable(pyDevice: PyDevice): Completable {
        return db.devicesDao().addDevice(pyDevice.toDevice())
    }

    private fun removeDeviceCompletable(pyDevice: PyDevice): Completable {
        return db.devicesDao().deleteDevice(pyDevice.toDevice())
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun onClickAdd() {
        if (!store.state.hasAddEmptyItem()) {
            dispatcher.post(DeviceListEvent.AddEmptyItem)
        }
    }

    val fabClickListener = View.OnClickListener {
        if (!store.state.hasAddEmptyItem()) {
            dispatcher.post(DeviceListEvent.AddEmptyItem)
        }
    }
}