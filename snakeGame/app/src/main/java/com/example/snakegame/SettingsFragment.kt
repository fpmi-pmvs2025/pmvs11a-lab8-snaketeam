package com.example.snakegame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SettingsFragment : Fragment() {
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: CityAdapter
    private lateinit var etCity: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        prefs = requireContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

        etCity = view.findViewById<EditText>(R.id.etCity)
        val btnFetch = view.findViewById<Button>(R.id.btnFetchWeather)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CityAdapter(CityStorage.getCities(requireContext())) { cityName ->
            etCity.setText(cityName)
            fetchWeather(cityName)
        }
        recyclerView.adapter = adapter

        // Load last used city
        etCity.setText(prefs.getString("last_city", "London"))

        btnFetch.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWeather(city)
                prefs.edit().putString("last_city", city).apply()
            } else {
                Toast.makeText(context, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            (activity as MainActivity).navigateToStart()
        }

        return view
    }

    private fun fetchWeather(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherApiService::class.java)
        service.getCurrentWeather(city).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()?.current
                    val weatherCode = response.body()?.current?.condition?.code ?: 1000
                    val weatherCategory = mapWeatherCode(weatherCode)
                    prefs.edit().putString("current_weather", weatherCategory).apply()
                    //
                    val temperature = weather?.temp_c ?: 0.0
                    CityStorage.saveCity(requireContext(), City(city, weatherCategory, temperature))
                    etCity.setText(city)
                    adapter.updateCities(CityStorage.getCities(requireContext()))

                    //
                    Toast.makeText(context, "Weather updated: $weatherCategory", Toast.LENGTH_SHORT).show()
                    if (weather != null) {
                        showWeatherDialog(city, weather)
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch weather", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showWeatherDialog(city: String, weather: CurrentWeather) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Weather in $city")
            .setMessage(
                "Temperature: ${weather.temp_c}°C\n" +
                        "Condition: ${weather.condition.text}"
            )
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    fun mapWeatherCode(code: Int): String {
        return when (code) {
            // Sunny
            1000 -> "sunny"

            // Cloudy
            1003, 1006, 1009 -> "cloudy"

            // Rain
            1063, 1069, 1072, 1087, 1150, 1153, 1168, 1171, 1180, 1183,
            1186, 1189, 1192, 1195, 1198, 1201, 1204, 1207, 1240, 1243,
            1246, 1249, 1252, 1273, 1276 -> "rain"

            // Snow
            1066, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222,
            1225, 1237, 1255, 1258, 1261, 1264, 1279, 1282 -> "snow"

            // Fog
            1030, 1135, 1147 -> "fog"

            // Default to cloudy for any unexpected codes
            else -> "cloudy"
        }
    }
}