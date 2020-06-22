package com.example.simpleweatherapp.ui.map

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.simpleweatherapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception


class MapFragment(location: String) : Fragment(), OnMapReadyCallback {

    private val loc = location
    private val client = OkHttpClient()
    private lateinit var googleMap: GoogleMap
    private var latlng = LatLng(13.621775, 123.194824)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        super.onCreate(savedInstanceState)

        map_view.onCreate(savedInstanceState)
        map_view.onResume()

        map_view.getMapAsync(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.let{
            googleMap = it

            googleMap.addMarker(MarkerOptions().position(latlng).title("Naga City"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))

            locateAddress()
        }
    }

    private fun locateAddress(): Boolean{

        googleMap.clear()

        val api = "19ac5eb086b102736d12de731215e7ec"
        val Weatherurl =
            "https://api.openweathermap.org/data/2.5/weather?lat=" +
                    "${latlng.latitude}&lon=${latlng.longitude}" +
                    "&appid=$api"


        Log.i("Url Access", Weatherurl)

        val request = Request.Builder()
            .url(Weatherurl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("api call","failed to locate address. exception: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("api call","address located. value: " + response.body()?.string())
                val geom = JSONObject(response.body()?.string())

                // TODO(could not download dependency) val model = gson.fromJson(geom,OpenWeatherData::class) parse weather data.
                //

            }
        })

        var addressList : List<Address> = emptyList()
        val geocoder =  Geocoder(activity)
        try {
            addressList = geocoder.getFromLocationName(loc,1)
            Log.i("Geocoder",addressList.toString())
        }
        catch (e: Exception){
            Log.i("Geocoder",e.toString())
        }

        if (addressList.isEmpty()){
            Log.i("Geocoder","Empty Array")
            //TODO(Implement Current location tracker)
        }
        else{
            latlng  = LatLng(addressList[0].latitude,addressList[0].longitude)
        }


        googleMap.addMarker(MarkerOptions().position(latlng).title(loc))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,10.0f))


        return true
    }

    data class OpenWeatherData(
        val coord: LatLng,
        val weather: Weather,
        val base: String,
        val main: Main,
        val wind: Wind,
        val clouds: Clouds,
        val dt: Long,
        val sys: Sys,
        val timezone: TimeZone
    )
    data class TimeZone(
        val timezone: Int,
        val id: Long,
        val name: String,
        val cod: Int
    )
    data class Sys(
        val country: String,
        val sunrise: Long,
        val sunset: Long
    )
    data class Clouds(
        val all: Int
    )
    data class Wind(
        val speed: Double,
        val deg:  Int
    )
    data class Main(
        val temp: Double,
        val feel: Double,
        val temp_min: Double,
        val temp_max: Double,
        val pressure: Int,
        val humidity:  Int,
        val sea_level: Int,
        val grnd_level: Int
    )
    data class Weather(
        val id: Int,
        val main: String,
        val desc: String,
        val icon: String
    )
    companion object {
        val TAG = MapFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(location: String) = MapFragment(location)
    }

}