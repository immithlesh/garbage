package com.garbagecollection.network
import android.content.Context
import com.garbagecollection.common.GCCommon.Companion.deviceId
import com.garbagecollection.common.GCCommon.Companion.deviceType
import com.garbagecollection.common.GCCommon.Companion.notificationToken
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {

    private var retrofit: Retrofit? = null
    private val requestTimeout = 120
    private var okHttpClient: OkHttpClient? = null

    fun getClient(context: Context): Retrofit {
//                .baseUrl("http://68.183.133.217:8080/api/v1/")

        if (okHttpClient == null)
            initOkHttp(context)
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl("http://3.19.98.166:8080/api/v1/")
                .client(okHttpClient!!)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    private fun initOkHttp(context: Context) {
        val httpClient = OkHttpClient().newBuilder()
            .connectTimeout(requestTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(requestTimeout.toLong(), TimeUnit.SECONDS)
            .writeTimeout(requestTimeout.toLong(), TimeUnit.SECONDS)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        httpClient.addInterceptor(interceptor)

        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .addHeader("basicAuth",  "acb8dbc6-fc09-11eb-9a03-0242ac130003")
                .addHeader("deviceId",  deviceId)
                .addHeader("notificationToken",notificationToken)
                .addHeader("deviceType",deviceType)

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        okHttpClient = httpClient.build()
    }

}