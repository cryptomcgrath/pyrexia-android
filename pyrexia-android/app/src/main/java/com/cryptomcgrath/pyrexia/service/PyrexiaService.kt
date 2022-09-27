package com.cryptomcgrath.pyrexia.service

import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.model.toStatList
import com.cryptomcgrath.pyrexia.thermostat.BASE_URL
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

internal class PyrexiaService() {

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().build())
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
}