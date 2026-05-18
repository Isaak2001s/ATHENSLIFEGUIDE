package com.athens.lifeguide.data.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ══════════════════════════════════════════════════
//  RESPONSE MODELS
// ══════════════════════════════════════════════════

data class AqiFeedResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data")   val data: AqiFeedData?
)

data class AqiFeedData(
    @SerializedName("aqi")  val aqi: Int,
    @SerializedName("idx")  val idx: Int,
    @SerializedName("city") val city: AqiCity,
    @SerializedName("iaqi") val iaqi: AqiComponents?,
    @SerializedName("time") val time: AqiTime?
)

data class AqiCity(
    @SerializedName("name") val name: String,
    @SerializedName("geo")  val geo: List<Double>?
)

data class AqiComponents(
    @SerializedName("pm25") val pm25: AqiValue?,
    @SerializedName("pm10") val pm10: AqiValue?,
    @SerializedName("o3")   val o3:   AqiValue?,
    @SerializedName("no2")  val no2:  AqiValue?,
    @SerializedName("so2")  val so2:  AqiValue?,
    @SerializedName("co")   val co:   AqiValue?
)

data class AqiValue(@SerializedName("v") val v: Double)

data class AqiTime(
    @SerializedName("s")  val s: String?,
    @SerializedName("tz") val tz: String?
)

data class AqiMapResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data")   val data: List<AqiMapStation>?
)

data class AqiMapStation(
    @SerializedName("uid")     val uid: Int,
    @SerializedName("aqi")     val aqi: String,
    @SerializedName("station") val station: AqiStationInfo
)

data class AqiStationInfo(
    @SerializedName("name") val name: String,
    @SerializedName("geo")  val geo: List<Double>?
)

// ══════════════════════════════════════════════════
//  SERVICE INTERFACE
// ══════════════════════════════════════════════════

interface AqiApiService {
    /** Feed by city slug, e.g. "athens" */
    @GET("feed/{city}/")
    suspend fun feedByCity(
        @Path("city")  city: String,
        @Query("token") token: String
    ): AqiFeedResponse

    /** Feed by station UID */
    @GET("feed/@{uid}/")
    suspend fun feedByStation(
        @Path("uid")   uid: Int,
        @Query("token") token: String
    ): AqiFeedResponse

    /** Stations in bounding box "lat1,lng1,lat2,lng2" */
    @GET("map/bounds/")
    suspend fun stationsInBounds(
        @Query("latlng")   latlng: String,
        @Query("token")    token: String,
        @Query("networks") networks: String = "all"
    ): AqiMapResponse
}

// ══════════════════════════════════════════════════
//  SINGLETON CLIENT
// ══════════════════════════════════════════════════

object AqiClient {
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: AqiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.waqi.info/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AqiApiService::class.java)
    }
}
