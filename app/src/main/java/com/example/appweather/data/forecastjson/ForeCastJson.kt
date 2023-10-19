package com.example.appweather.data.forecastjson

data class ForeCastJson(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<ForeCastData>,
    val message: Int
)