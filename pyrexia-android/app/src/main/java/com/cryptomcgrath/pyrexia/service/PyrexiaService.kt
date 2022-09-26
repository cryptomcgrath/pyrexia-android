package com.cryptomcgrath.pyrexia.service

import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.toProgramList
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PyrexiaService() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val client = retrofit.create(PyrexiaApi::class.java)

    fun getProgramsList(baseUrl: String): Single<List<Program>> {
        return client.getPrograms(baseUrl)
            .map {
                it.toProgramList()
            }
    }

}