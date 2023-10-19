package com.example.appweather.data.forecastjson

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)