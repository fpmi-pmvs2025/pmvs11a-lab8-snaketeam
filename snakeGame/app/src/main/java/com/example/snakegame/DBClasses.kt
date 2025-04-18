package com.example.snakegame

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class City(
    val name: String,
    val weatherCondition: String,
    val temperature: Number,
    val timestamp: Long = System.currentTimeMillis()
)

object CityStorage {
    private const val PREFS_KEY = "cities"
    private const val MAX_CITIES = 10
    private val gson = Gson()

    fun saveCity(context: Context, newCity: City) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val cities = getCities(context).toMutableList()

        // Remove duplicates and old entries
        cities.removeAll { it.name.equals(newCity.name, ignoreCase = true) }
        cities.add(newCity)

        // Keep only last 10
        while (cities.size > MAX_CITIES) {
            cities.removeAt(0)
        }

        prefs.edit().putString(PREFS_KEY, gson.toJson(cities)).apply()
    }

    fun getCities(context: Context): List<City> {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val json = prefs.getString(PREFS_KEY, "") ?: ""
        return try {
            gson.fromJson(json, Array<City>::class.java).toList().sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class CityAdapter(private var cities: List<City>,
                  private val onCityClick: (String) -> Unit) : RecyclerView.Adapter<CityAdapter.ViewHolder>() {
    private val weatherIcons = mapOf(
        "sunny" to R.drawable.sunny,
        "cloudy" to R.drawable.cloud,
        "rain" to R.drawable.rain,
        "snow" to R.drawable.snow,
        "fog" to R.drawable.fog
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cityName: TextView = view.findViewById(R.id.cityName)
        val weatherIcon: ImageView = view.findViewById(R.id.weatherIcon)
        val condition: TextView = view.findViewById(R.id.condition)
        val temperature: TextView = view.findViewById(R.id.temperature)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = cities[position]
        holder.cityName.text = city.name
        holder.condition.text = city.weatherCondition
        holder.weatherIcon.setImageResource(weatherIcons[city.weatherCondition] ?: R.drawable.cloud)
        holder.temperature.text = "${city.temperature}°C"
        holder.timestamp.text = formatTimestamp(city.timestamp)
        holder.itemView.setOnClickListener {
            onCityClick(city.name)
        }
    }
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    override fun getItemCount() = cities.size

    fun updateCities(newCities: List<City>) {
        cities = newCities
        notifyDataSetChanged()
    }
}