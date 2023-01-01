package com.cryptomcgrath.pyrexia.service

import android.app.Application
import com.cryptomcgrath.pyrexia.db.PyrexiaDb
import com.cryptomcgrath.pyrexia.db.toDevice
import com.cryptomcgrath.pyrexia.db.toPyDevice
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.model.toAddStatDto
import com.cryptomcgrath.pyrexia.model.toControlUpdateDto
import com.cryptomcgrath.pyrexia.model.toControlsList
import com.cryptomcgrath.pyrexia.model.toHistoryList
import com.cryptomcgrath.pyrexia.model.toSensorList
import com.cryptomcgrath.pyrexia.model.toSensorUpdateDto
import com.cryptomcgrath.pyrexia.model.toStatList
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

internal class PyrexiaService(application: Application, var pyDevice: PyDevice) {
    private val db = PyrexiaDb.getDatabase(application)

    private val networkFlipperPlugin = AndroidFlipperClient.getInstance(application)
        .getPlugin<NetworkFlipperPlugin>(NetworkFlipperPlugin.ID)

    private val httpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(Interceptor { chain ->
            val original = chain.request()
            val newRequest = original.newBuilder().apply {
                tokenMap[pyDevice.uid]?.let {
                    this.addHeader(HEADER_TOKEN, it)
                }
            }.addHeader("Platform", "android")
            chain.proceed(newRequest.build())
        }).apply {
            if (networkFlipperPlugin != null) {
                val flipperInterceptor = FlipperOkhttpInterceptor(networkFlipperPlugin)
                this.addNetworkInterceptor(flipperInterceptor)
            }
        }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(pyDevice.baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    private val client = retrofit.create(PyrexiaApi::class.java)

    fun getStatList(): Single<List<VirtualStat>> {
        return client.getStatList()
            .map {
                it.toStatList()
            }
    }

    fun addStat(program: Program): Completable {
        return client.addStat(
            program.toAddStatDto()
        )
    }

    fun updateStat(program: Program): Completable {
        return client.updateStat(program.id, program.toAddStatDto())
    }

    fun statIncrease(id: Int): Completable {
        return client.statIncrease(id)
    }

    fun statDecrease(id: Int): Completable {
        return client.statDecrease(id)
    }

    fun statEnable(id: Int): Completable {
        return client.statEnable(id)
    }

    fun statDisable(id: Int): Completable {
        return client.statDisable(id)
    }

    fun refill(id: Int): Completable {
        return client.refill(id)
    }

    fun getHistory(offset: Int, limit: Int, programId: Int?): Single<List<History>> {
        return client.getHistory(offset, limit, programId)
            .map {
                it.toHistoryList()
            }
    }

    fun getSensors(): Single<List<Sensor>> {
        return client.getSensors()
            .map {
                it.toSensorList()
            }
    }

    fun getControls(): Single<List<Control>> {
        return client.getControls()
            .map {
                it.toControlsList()
            }
    }

    fun addSensor(sensor: Sensor): Completable {
        return client.addSensor(sensor.toSensorUpdateDto())
    }

    fun updateSensor(sensor: Sensor): Completable {
        return client.updateSensor(sensor.id, sensor.toSensorUpdateDto())
    }

    fun deleteSensor(sensor: Sensor): Completable {
        return client.deleteSensor(sensor.id)
    }

    fun addControl(control: Control): Completable {
        return client.addControl(control.toControlUpdateDto())
    }

    fun updateControl(control: Control): Completable {
        return client.updateControl(control.id, control.toControlUpdateDto())
    }

    fun deleteControl(control: Control): Completable {
        return client.deleteControl(control.id)
    }

    fun login(email: String, password: String): Completable {
        return client.login(
            LoginRequestDto(
                email = email,
                password = password
            )
        ).flatMapCompletable {
            val updatedPyDevice = pyDevice.copy(
                token = it.token
            )
            pyDevice = updatedPyDevice
            tokenMap[pyDevice.uid] = pyDevice.token
            db.devicesDao().updateDevice(pyDevice.toDevice())
        }
    }

    fun isLoggedIn(): Single<Boolean> {
        return db.devicesDao().getDevice(pyDevice.uid)
            .flatMap {
                pyDevice = it.toPyDevice()
                tokenMap[it.uid] = it.token
                Single.just(it.token.isNotEmpty())
            }
    }

    companion object {
        val tokenMap = mutableMapOf<Int, String>()
    }
}

private const val HEADER_TOKEN = "x-access-token"

internal  fun Throwable.isUnauthorized(): Boolean {
    return this is HttpException && setOf(401,403).contains(this.code())
}