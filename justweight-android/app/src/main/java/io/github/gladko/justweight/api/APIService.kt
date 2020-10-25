package io.github.gladko.justweight.api

import io.github.gladko.justweight.db.Measurement
import io.github.gladko.justweight.db.User
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*
import rx.observables.BlockingObservable


internal interface APIService {

    @GET("measurements")
    fun getListMeasurements(): Observable<ArrayList<Measurement>>

    @GET("profile")
    fun getProfile(): Observable<ArrayList<User>>

    @GET("signature")
    fun getSignature(): Observable<String>

    @POST("measurements/")
    fun addMeasurements(@Body measurement: Measurement): Single<Measurement>

}