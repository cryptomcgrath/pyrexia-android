package com.cryptomcgrath.pyrexia.service

import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.Sensor
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface PyrexiaApi {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @GET("/stat/list")
    fun getStatList(): Single<GetStatListDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("/stat/{id}/increase")
    fun statIncrease(@Path("id") id: Int): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("/stat/{id}/decrease")
    fun statDecrease(@Path("id") id: Int): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("/stat/{id}/enable")
    fun statEnable(@Path("id") id: Int): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("/stat/{id}/disable")
    fun statDisable(@Path("id") id: Int): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @GET("/history")
    fun getHistory(@Query("offset") offset: Int,
                   @Query("limit") limit: Int,
                   @Query("program_id") program_id: Int?): Single<GetHistoryDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @GET("/sensors")
    fun getSensors(): Single<GetSensorsDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @GET("/controls")
    fun getControls(): Single<GetControlsDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @PATCH("/sensors/{id}")
    fun updateSensor(@Path("id") id: String,
                     @Body sensor: SensorUpdateDto): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("/sensors")
    fun addSensor(@Body sensor: SensorUpdateDto): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @DELETE("/sensors/{id}")
    fun deleteSensor(@Path("id") id: String): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @PATCH("/controls/{id}")
    fun updateControl(@Path("id") id: String,
                      @Body control: ControlUpdateDto): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("/controls")
    fun addControl(@Body control: ControlUpdateDto): Completable

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @DELETE("/controls/{id}")
    fun deleteControl(@Path("id") id: String): Completable
}