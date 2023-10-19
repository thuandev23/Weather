package com.example.appweather


import android.annotation.SuppressLint
import com.google.android.gms.location.LocationRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appweather.adapter.RvAdapter
import com.example.appweather.data.MockApi
import com.example.appweather.data.forecastjson.ForeCastData
import com.example.appweather.data.weatherjson.JsonWeather
import com.example.appweather.databinding.ActivityMainBinding
import com.example.appweather.databinding.BottomSheetLayoutBinding
import com.example.appweather.utils.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.HttpException
import java.io.IOException
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var sheetLayoutBinding: BottomSheetLayoutBinding
    private lateinit var dialog: BottomSheetDialog
    private var PERMISSION_ID = 1
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        sheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        dialog.setContentView(sheetLayoutBinding.root)

        fetchWeatherData("ho chi minh")
        searchCity()

        binding.tvForecast.setOnClickListener{
//            openDialog()
            Toast.makeText(applicationContext, "Bạn đã nhấp vào", Toast.LENGTH_SHORT).show()
            println(getForeCase("ho chi minh"))
        }


    }


    private fun openDialog() {
        getForeCase("ho chi minh")
        sheetLayoutBinding.rvForecast.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@MainActivity,1, RecyclerView.HORIZONTAL, false)

        }
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }
    private fun getForeCase(cityName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.retrofit.getForeCast(
                    cityName,
                    "91e251c5758236ae03485dc5fa6b8fea",
                    "metric",
                )
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "app error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(applicationContext, "http error ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {

                    val data = response.body()!!

                    var forecastArray = arrayListOf<ForeCastData>()
                    forecastArray = data.list as ArrayList<ForeCastData>

                    val adapter = RvAdapter(forecastArray)
                    sheetLayoutBinding.rvForecast.adapter = adapter
                    sheetLayoutBinding.tvSheet.text = "Five days forecast ${data.city}"

                }
            }
        }
    }

    private fun searchCity() {
        val searchView = binding.searchView2

        searchView.setOnQueryTextListener(object :android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }
    private fun fetchWeatherData(cityName:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(MockApi::class.java)

        val response = retrofit.getWeatherData(cityName,"91e251c5758236ae03485dc5fa6b8fea","metric")
        response.enqueue(object : Callback<JsonWeather> {
            override fun onResponse(call: Call<JsonWeather>, response: Response<JsonWeather>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null){
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise =responseBody.sys.sunrise.toLong()
                    val sunSet =responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min
                    val countries = responseBody.sys.country
                    val description = responseBody.weather.firstOrNull()?.description?:"null"
                    val clouds = responseBody.clouds.all
                    binding.temp.text = "$temperature °C"
                    binding.weather.text = "$description : $clouds %"
                    binding.maxTerm.text = "Max Temp: $maxTemp °C"
                    binding.minTerm.text = "Min Temp: $minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunRise.text = "${time(sunRise)}"
                    binding.sunSet.text = "${time(sunSet)}"
                    binding.sea.text = "$seaLevel hPa"
                    binding.conditions.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityName.text = "$cityName - $countries"
                    changeImageAccordingToWeatherCondition(condition)
                }
            }

            override fun onFailure(call: Call<JsonWeather>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

    }
    @SuppressLint("ResourceAsColor")
    private fun changeImageAccordingToWeatherCondition(conditions:String) {
        when (conditions){
            "Sunny", "Clear"->{
                binding.root.setBackgroundResource(R.drawable.sunnybg)
                binding.temp.setTextColor(R.color.black)
                binding.weather.setTextColor(R.color.black)
                binding.maxTerm.setTextColor(R.color.black)
                binding.minTerm.setTextColor(R.color.black)
                binding.day.setTextColor(R.color.black)
                binding.date.setTextColor(R.color.black)
                binding.cityName.setTextColor(R.color.black)
                //
                binding.humidity.setTextColor(R.color.black)
                binding.humidityTxt.setTextColor(R.color.black)
                binding.windSpeed.setTextColor(R.color.black)
                binding.windSpeedTxt.setTextColor(R.color.black)
                binding.sunRise.setTextColor(R.color.black)
                binding.sunRiseTxt.setTextColor(R.color.black)
                binding.sunSet.setTextColor(R.color.black)
                binding.sunSetTxt.setTextColor(R.color.black)
                binding.sea.setTextColor(R.color.black)
                binding.seaTxt.setTextColor(R.color.black)
                binding.conditions.setTextColor(R.color.black)
                binding.conditionsTxt.setTextColor(R.color.black)
            }
            "Clear Sky"->{
                binding.root.setBackgroundResource(R.drawable.clearsky)
                binding.lottieLayerName.setAnimation(R.raw.sunnyjs)
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Fog", "Few Clouds", "Thunderstorm"->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieLayerName.setAnimation(R.raw.cloud)
            }
            "LIGHT RAIN", "Drizzle", "Moderate Rain", "Rain", "Showers", "Heavy Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieLayerName.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard", "Snow" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieLayerName.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_bg)
                binding.lottieLayerName.setAnimation(R.raw.sunnyjs)
                binding.humidity.setTextColor(R.color.black)
                binding.humidityTxt.setTextColor(R.color.black)
                binding.windSpeed.setTextColor(R.color.black)
                binding.windSpeedTxt.setTextColor(R.color.black)
                binding.sunRise.setTextColor(R.color.black)
                binding.sunRiseTxt.setTextColor(R.color.black)
                binding.sunSet.setTextColor(R.color.black)
                binding.sunSetTxt.setTextColor(R.color.black)
                binding.sea.setTextColor(R.color.black)
                binding.seaTxt.setTextColor(R.color.black)
                binding.conditions.setTextColor(R.color.black)
                binding.conditionsTxt.setTextColor(R.color.black)

            }
        }
        binding.lottieLayerName.playAnimation()
    }
    private fun date(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
    private fun time(timestamp:Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }
    fun dayName(timestamp:Long):String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}



