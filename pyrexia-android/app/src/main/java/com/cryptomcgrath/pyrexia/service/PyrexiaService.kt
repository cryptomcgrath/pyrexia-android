package com.cryptomcgrath.pyrexia.service

import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.model.toProgramList
import com.cryptomcgrath.pyrexia.model.toProgramRunList
import com.cryptomcgrath.pyrexia.thermostat.BASE_URL
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

    fun getProgramsList(baseUrl: String): Single<List<Program>> {
        return client.getPrograms()
            .map {
                it.toProgramList()
            }
    }

    fun getProgramsRunList(): Single<List<ProgramRun>> {
        return client.getProgramsRun()
            .map {
                it.toProgramRunList()
            }
    }
}