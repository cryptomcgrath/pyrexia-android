package com.cryptomcgrath.pyrexia.service

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface PyrexiaApi {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "Platform: android")
    @GET("/programs")
    fun getPrograms(@Url baseUrl: String): Single<GetProgramsDto>

}