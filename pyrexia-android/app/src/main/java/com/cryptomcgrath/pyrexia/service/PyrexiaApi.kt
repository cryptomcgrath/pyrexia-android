package com.cryptomcgrath.pyrexia.service

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

internal interface PyrexiaApi {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "Platform: android")
    @GET("/stat/list")
    fun getStatList(): Single<GetStatListDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "Platform: android")
    @POST("/stat/{id}/increase")
    fun statIncrease(@Path("id") id: Int): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "Platform: android")
    @POST("/stat/id}/decrease")
    fun statDecrease(@Path("id") id: Int): Completable
}