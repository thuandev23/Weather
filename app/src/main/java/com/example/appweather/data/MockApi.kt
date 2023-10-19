package com.example.appweather.data

import com.example.appweather.data.forecastjson.ForeCastJson
import com.example.appweather.data.weatherjson.JsonWeather
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MockApi {
    @GET("weather")
    fun getWeatherData(
        @Query("q") city:String,
        @Query("appid") appid:String,
        @Query("units") units:String
    ): Call<JsonWeather>

    @GET("forecast")
    fun getForeCast(
        @Query("q") city:String,
        @Query("appid") appid:String,
        @Query("units") units:String
    ): Response<ForeCastJson>
}