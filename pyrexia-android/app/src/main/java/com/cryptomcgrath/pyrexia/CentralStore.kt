package com.cryptomcgrath.pyrexia

import SingletonHolder
import android.app.Application
import android.util.Log
import com.cryptomcgrath.pyrexia.db.PyrexiaDb
import com.cryptomcgrath.pyrexia.db.toDevice
import com.cryptomcgrath.pyrexia.db.toPyDeviceList
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.HistoryPage
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.cryptomcgrath.pyrexia.service.isUnauthorized
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.ReducerFun
import com.edwardmcgrath.blueflux.core.RxStore
import com.edwardmcgrath.blueflux.core.State
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

internal class CentralStore private constructor(val app: Application): DevicesRepo {

    val store = RxStore.create(centralReducerFun)
    val dispatcher = Dispatcher.create(store)
    val state get() = store.state

    private val serviceMap: MutableMap<Int, PyrexiaService> = hashMapOf()

    private val db = PyrexiaDb.getDatabase(app)
    private val disposables = CompositeDisposable()
    private var autoRefreshDisposable: Disposable? = null

    init {
        reactToDispatchedEvents()
        refreshDeviceList()
    }

    private fun getPyrexiaService(pyDevice: PyDevice): PyrexiaService {
        var service = serviceMap[pyDevice.uid]
        if (service == null) {
            service = PyrexiaService(
                application = app,
                pyDevice = pyDevice
            )
            serviceMap[pyDevice.uid] = service
        }
        return service
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

    override fun saveSensor(pyDevice: PyDevice, sensor: Sensor): Completable {
        val pyrexiaService = getPyrexiaService(pyDevice)
        return if (sensor.id == 0) {
            pyrexiaService.addSensor(sensor)
        } else {
            pyrexiaService.updateSensor(sensor)
        }.doOnSubscribe {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, true))
        }.doOnComplete {
            refreshDeviceData(pyDevice)
        }.doOnError {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, false))
        }
    }

    override fun deleteSensor(pyDevice: PyDevice, sensor: Sensor): Completable {
        val pyrexiaService = getPyrexiaService(pyDevice)
        return Single.just(sensor.id)
            .flatMapCompletable {
                if (it > 0) {
                    pyrexiaService.deleteSensor(sensor)
                        .doOnSubscribe {
                            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, true))
                        }
                } else {
                    Completable.complete()
                }
            }.doOnComplete {
                fetchDeviceConfig(pyDevice)
            }
    }

    override fun saveControl(pyDevice: PyDevice, control: Control): Completable {
        val pyrexiaService = getPyrexiaService(pyDevice)
        return if (control.id == 0) {
            pyrexiaService.addControl(control)
        } else {
            pyrexiaService.updateControl(control)
        }.doOnSubscribe {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, true))
        }.doOnComplete {
            refreshDeviceData(pyDevice)
        }.doOnError {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, false))
        }
    }

    override fun deleteControl(pyDevice: PyDevice, control: Control): Completable {
        val pyrexiaService = getPyrexiaService(pyDevice)
        return if (control.id > 0) {
            pyrexiaService.deleteControl(control)
        } else {
            Completable.complete()
                .doOnComplete {
                    fetchDeviceConfig(pyDevice)
                }
        }
    }

    private fun Completable.handleLoading(pyDevice: PyDevice): Completable {
        return this.doOnSubscribe {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, true))
        }.doOnComplete {
            refreshDeviceData(pyDevice)
        }.doOnError {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, false))
        }
    }

    override fun saveStat(pyDevice: PyDevice, program: Program): Completable {
        val pyrexiaService = getPyrexiaService(pyDevice)
        return if (program.id == 0) {
            pyrexiaService.addStat(program)
        } else {
            pyrexiaService.updateStat(program)
        }.doOnSubscribe {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, true))
        }.doOnComplete {
            refreshDeviceData(pyDevice)
        }.doOnError {
            dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, false))
        }
    }

    //private fun deleteStat(pyDevice: PyDevice, stat: VirtualStat): Completable {
    //    return getPyrexiaService(pyDevice)
    //    //getPyrexiaService(deviceId).
    //}

    private fun fetchDeviceConfig(pyDevice: PyDevice) {
        val pyrexiaService = getPyrexiaService(pyDevice)
        val deviceId = pyDevice.uid
        Singles.zip(
            pyrexiaService.getStatList(),
            pyrexiaService.getSensors(),
            pyrexiaService.getControls()
        ).doOnSubscribe {
            if (state.getDeviceState(pyDevice).stats.isEmpty()) {
                dispatcher.post(CentralEvent.SetLoading(deviceId, true))
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { (stats, sensors, controls) ->
                    dispatcher.post(CentralEvent.NewDeviceConfig(deviceId, stats, sensors, controls))
                },
                onError = {
                    if (it.isUnauthorized()) {
                        dispatcher.post(CentralEvent.GoToLogin(pyDevice))
                    } else {
                        if (state.getDeviceState(pyDevice).stats.isEmpty()) {
                            dispatcher.post(CentralEvent.NetworkError(deviceId, it, true))
                        } else {
                            dispatcher.post(CentralEvent.ConnectionError(deviceId, it))
                        }
                    }
                }
            ).addTo(disposables)
    }

    fun refreshDeviceData(pyDevice: PyDevice) {
        fetchDeviceConfig(pyDevice)
    }

    fun setupAutoRefresh(pyDevice: PyDevice) {
        refreshDeviceData(pyDevice)
        autoRefreshDisposable = Observable.interval(AUTO_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    Log.d(TAG, "refreshing data")
                    refreshDeviceData(pyDevice)
                },
                onError = {
                    // ignore
                }
            )
    }

    fun cancelAutoRefresh() {
        autoRefreshDisposable?.dispose()
    }

    override fun shutdownDevice(pyDevice: PyDevice): Completable {
        return getPyrexiaService(pyDevice).shutdown()
    }
    override fun increaseTemp(pyDevice: PyDevice, statId: Int): Completable {
        return getPyrexiaService(pyDevice).statIncrease(statId)
            .doOnSubscribe {
                dispatcher.post(CentralEvent.SetUpdating(pyDevice.uid, true))
            }.doOnSuccess {
                dispatcher.post(CentralEvent.UpdateStat(pyDevice.uid, it))
            }.ignoreElement()
    }

    override fun decreaseTemp(pyDevice: PyDevice, statId: Int): Completable {
        val deviceId = pyDevice.uid
        return getPyrexiaService(pyDevice).statDecrease(statId)
            .doOnSubscribe {
                dispatcher.post(CentralEvent.SetUpdating(deviceId, true))
            }.doOnSuccess {
                dispatcher.post(CentralEvent.UpdateStat(pyDevice.uid, it))
            }.ignoreElement()
    }

    override fun enableStat(pyDevice: PyDevice, statId: Int): Completable {
        val deviceId = pyDevice.uid
        return getPyrexiaService(pyDevice).statEnable(statId)
            .doOnSubscribe {
                dispatcher.post(CentralEvent.SetUpdating(deviceId, true))
            }
            .doOnSuccess {
                dispatcher.post(CentralEvent.UpdateStat(deviceId, it))
            }.ignoreElement()
    }

    override fun disableStat(pyDevice: PyDevice, statId: Int): Completable {
        val deviceId = pyDevice.uid
        return getPyrexiaService(pyDevice).statDisable(statId)
            .doOnSubscribe {
                dispatcher.post(CentralEvent.SetUpdating(deviceId, true))
            }.doOnSuccess {
                dispatcher.post(CentralEvent.UpdateStat(deviceId, it))
            }.ignoreElement()
    }

    override fun refill(pyDevice: PyDevice, controlId: Int): Completable {
        val deviceId = pyDevice.uid
        return getPyrexiaService(pyDevice).refill(controlId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                dispatcher.post(CentralEvent.SetUpdating(deviceId, true))
            }.doOnComplete {
                refreshDeviceData(pyDevice)
            }
    }

    override fun fetchHistory(pyDevice: PyDevice, statId: Int, startTs: Int?, endTs: Int?): Single<HistoryPage> {
        val deviceId = pyDevice.uid
        val HISTORY_FETCH_LIMIT = 500
        val OFFSET = 0
        return getPyrexiaService(pyDevice).getHistoryPage(
            offset = OFFSET,
            limit = HISTORY_FETCH_LIMIT,
            statId = statId,
            startTs = startTs,
            endTs = endTs
        ).doOnSuccess {
            dispatcher.post(CentralEvent.NewHistory(deviceId, it))
        }
    }

    override fun loginToDevice(pyDevice: PyDevice, email: String, password: String): Completable {
        return getPyrexiaService(pyDevice).login(email, password)
    }

    companion object : SingletonHolder<CentralStore, Application>(::CentralStore)
}

internal data class CentralState(
    val deviceList: List<PyDevice> = listOf(),
    val deviceStateMap: Map<Int, DeviceState> = hashMapOf(),
    val databaseError: Throwable? = null
): State {
    data class DeviceState(
        val sensors: List<Sensor> = emptyList(),
        val controls: List<Control> = emptyList(),
        val stats: List<VirtualStat> = emptyList(),
        val updating: Boolean = false,
        val loading: Boolean = false,
        val historyPages: List<HistoryPage> = listOf(),
        val connectionError: Throwable? = null
    ) {
        fun getMinHistoryPageForStat(statId: Int): HistoryPage? {
            return historyPages.filter {
                it.statId == statId && it.minTs != null
            }.minByOrNull {
                it.minTs ?: 0
            }
        }
    }

    fun hasAddEmptyItem(): Boolean {
        return deviceList.any {
            it.name.isEmpty()
        }
    }

    fun getDeviceState(deviceId: Int): DeviceState {
        return deviceStateMap[deviceId] ?: DeviceState()
    }

    fun getDeviceState(pyDevice: PyDevice): DeviceState {
        return deviceStateMap[pyDevice.uid] ?: DeviceState()
    }
}

internal val centralReducerFun: ReducerFun<CentralState> = { inState, event ->
    val state = inState ?: CentralState()

    when (event) {
        is CentralEvent.NewDeviceList -> {
            val newMap = state.deviceStateMap.filter {
                it.key in event.deviceList.map { it.uid }
            }

            state.copy(
                deviceList = event.deviceList,
                deviceStateMap = newMap,
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

        is CentralEvent.NewDeviceConfig -> {
            val newDeviceStateMap = state.deviceStateMap.toMutableMap()

            newDeviceStateMap[event.deviceId] = (newDeviceStateMap[event.deviceId] ?: CentralState.DeviceState()).copy(
                loading = false,
                updating = false,
                stats = event.stats,
                sensors = event.sensors,
                controls = event.controls,
                connectionError = null
            )
            state.copy(
                deviceStateMap = newDeviceStateMap
            )
        }

        is CentralEvent.NetworkError -> {
            val newDeviceMap = state.deviceStateMap.toMutableMap()
            newDeviceMap[event.deviceId]?.copy(
                loading = false,
                updating = false
            )?.let {
                newDeviceMap[event.deviceId] = it
            }
            state.copy(
                deviceStateMap = newDeviceMap
            )
        }

        is CentralEvent.SetLoading -> {
            val newDeviceMap = state.deviceStateMap.toMutableMap()
            (newDeviceMap[event.deviceId] ?: CentralState.DeviceState()).copy(
                loading = event.loading
            ).let {
                newDeviceMap[event.deviceId] = it
            }
            state.copy(
                deviceStateMap = newDeviceMap
            )
        }

        is CentralEvent.SetUpdating -> {
            val newDeviceMap = state.deviceStateMap.toMutableMap()
            newDeviceMap[event.deviceId]?.copy(
                updating = event.updating
            )?.let {
                newDeviceMap[event.deviceId] = it
            }
            state.copy(
                deviceStateMap = newDeviceMap
            )
        }

        is CentralEvent.UpdateStat -> {
            val newDeviceMap = state.deviceStateMap.toMutableMap()
            val deviceState = state.getDeviceState(event.deviceId)
            val newStats = deviceState.stats.toMutableList()
            val idx = newStats.indexOfFirst {
                it.program.id == event.stat.program.id
            }
            if (idx != -1) {
                newStats[idx] = event.stat
            } else {
                newStats.add(event.stat)
            }

            newDeviceMap[event.deviceId]?.copy(
                stats = newStats,
                updating = false
            )?.let {
                newDeviceMap[event.deviceId] = it
            }
            state.copy(
                deviceStateMap = newDeviceMap,
            )
        }

        is CentralEvent.ConnectionError -> {
            val newDeviceMap = state.deviceStateMap.toMutableMap()
            newDeviceMap[event.deviceId]?.copy(
                connectionError = event.throwable,
                loading = false,
                updating = false
            )?.let {
                newDeviceMap[event.deviceId] = it
            }
            state.copy(
                deviceStateMap = newDeviceMap
            )
        }

        is CentralEvent.NewHistory -> {
            val newDeviceMap = state.deviceStateMap.toMutableMap()
            val newHistoryPages = newDeviceMap[event.deviceId]?.historyPages?.toMutableList()
            newHistoryPages?.add(event.historyPage)
            newDeviceMap[event.deviceId]?.copy(
                historyPages = newHistoryPages.orEmpty()
            )?.let {
                newDeviceMap[event.deviceId] = it
            }
            state.copy(
                deviceStateMap = newDeviceMap
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

    data class GoToLogin(val pyDevice: PyDevice): CentralEvent()
    data class NetworkError(val deviceId: Int,
                            val throwable: Throwable,
                            val finish: Boolean) : CentralEvent()

    data class ConnectionError(val deviceId: Int, val throwable: Throwable): CentralEvent()
    data class NewDeviceConfig(val deviceId: Int,
                           val stats: List<VirtualStat>,
                           val sensors: List<Sensor>,
                           val controls: List<Control>): CentralEvent()
    data class SetLoading(val deviceId: Int, val loading: Boolean): CentralEvent()
    data class SetUpdating(val deviceId: Int, val updating: Boolean): CentralEvent()

    data class RequestRefill(val pyDevice: PyDevice, val controlId: Int): CentralEvent()
    data class NewHistory(val deviceId: Int, val historyPage: HistoryPage): CentralEvent()

    data class UpdateStat(val deviceId: Int, val stat: VirtualStat): CentralEvent()
}

private const val TAG="CentralStore"
internal const val AUTO_REFRESH_INTERVAL = 15L
