package com.cryptomcgrath.pyrexia.service

import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.model.toControlsList
import com.cryptomcgrath.pyrexia.model.toHistoryList
import com.cryptomcgrath.pyrexia.model.toSensorList
import com.cryptomcgrath.pyrexia.model.toStatList
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

internal class PyrexiaService(pyDevice: PyDevice) {

    private val httpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(Interceptor { chain ->
            val original = chain.request()
            val newRequest = original.newBuilder().apply {
                if (pyDevice.token.isNotEmpty()) {
                    this.addHeader(HEADER_TOKEN, pyDevice.token)
                }
            }.addHeader(HEADER_API, API_KEY)
                .addHeader("Platform", "android")
            chain.proceed(newRequest.build())
        }).build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(pyDevice.baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    private val client = retrofit.create(PyrexiaApi::class.java)

    fun getStatList(): Single<List<ProgramRun>> {
        return client.getStatList()
            .map {
                it.toStatList()
            }
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
}

private const val HEADER_TOKEN = "token"
private const val HEADER_API = "apikey"
private const val API_KEY = "e4-5f-01-5b-f4-4f"