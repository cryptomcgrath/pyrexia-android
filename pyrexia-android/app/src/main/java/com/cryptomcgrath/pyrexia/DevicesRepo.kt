package com.cryptomcgrath.pyrexia

import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.HistoryPage
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import io.reactivex.Completable
import io.reactivex.Single

internal interface DevicesRepo {

    fun shutdownDevice(pyDevice: PyDevice): Completable

    fun refreshDeviceConfig(pyDevice: PyDevice): Completable

    fun refreshStats(pyDevice: PyDevice): Completable

    fun increaseTemp(pyDevice: PyDevice, statId: Int): Completable

    fun decreaseTemp(pyDevice: PyDevice, statId: Int): Completable

    fun enableStat(pyDevice: PyDevice, statId: Int): Completable

    fun disableStat(pyDevice: PyDevice, statId: Int): Completable

    fun refill(pyDevice: PyDevice, controlId: Int): Completable

    fun loginToDevice(pyDevice: PyDevice, email: String, password: String): Completable

    fun saveStat(pyDevice: PyDevice, program: Program): Completable

    fun deleteControl(pyDevice: PyDevice, control: Control): Completable

    fun saveControl(pyDevice: PyDevice, control: Control): Completable

    fun deleteSensor(pyDevice: PyDevice, sensor: Sensor): Completable

    fun saveSensor(pyDevice: PyDevice, sensor: Sensor): Completable

    fun fetchHistory(pyDevice: PyDevice, statId: Int, startTs: Int?, endTs: Int?): Single<HistoryPage>

}