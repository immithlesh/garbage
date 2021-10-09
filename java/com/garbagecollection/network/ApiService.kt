package com.garbagecollection.network

import com.garbagecollection.viewUI.GarbageCollectionPostDataNew
import com.garbagecollection.viewUI.map.FetchMarkerModel
import com.garbagecollection.viewUI.route.RouteModel
import com.garbagecollection.viewUI.weekday.WeekDayData
import io.reactivex.Single
import retrofit2.http.*

interface ApiService {

    @GET("week-days")
    fun getWeekDay(): Single<WeekDayData>

    @GET("routes")
    fun getRoute(): Single<RouteModel>

    @GET("filter")
    fun getFilterMap(
        @Query("type") type: String,
        @Query("route") route: String,
        @Query("selectedDay") selectedDay: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Single<FetchMarkerModel>

    @POST("collect-garbage")
    fun apiPostCollectedData(@Body body: ArrayList<GarbageCollectionPostDataNew>): Single<Any>

}
