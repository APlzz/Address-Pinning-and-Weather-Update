package com.example.simpleweatherapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.simpleweatherapp.MainActivity
import com.example.simpleweatherapp.R
import com.example.simpleweatherapp.ui.home.HomeFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_map.*
import okhttp3.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import java.util.*


class MapFragment(location: String) : Fragment(), OnMapReadyCallback , Callback{

    private val loc = location
    private val client = OkHttpClient()
    private lateinit var googleMap: GoogleMap
    private var latlng = LatLng(13.621775, 123.194824)
    private lateinit var weatherModel: OpenWeatherData
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCode = 2
    private lateinit var jsonOBJ: JSONObject


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        super.onCreate(savedInstanceState)

        fusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!

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

            locateAddress()

        }
    }

    private fun getWeather(): Boolean{
        val api = "19ac5eb086b102736d12de731215e7ec"
        val weatherUrl =
            "https://api.openweathermap.org/data/2.5/weather?lat=" +
                    "${latlng.latitude}&lon=${latlng.longitude}" +
                    "&appid=$api"


        Log.i("Url Access", weatherUrl)

        val request = Request.Builder()
            .url(weatherUrl)
            .build()

        client.newCall(request).enqueue(this)


        return true
    }

    override fun onFailure(call: Call, e: IOException) {
        Log.i("api call","failed to locate address. exception: $e")
    }

    override fun onResponse(call: Call, response: Response) {
        val json = response.body?.string()
        Log.i("api call", "address located. value: $json")

        jsonOBJ = JSONObject(json)
        updateWeatherText()
    }

    private fun updateWeatherText(){

        Log.i("JSON Parse",jsonOBJ.getString("weather").toString())
        Log.i("JSON Parse",jsonOBJ.getString("main").toString())
        //TODO(Display Weather Data to map)
//        val weatherView = TextView(context)
//        weatherView.textSize = 30f
//        weatherView.text  = jsonOBJ.getString("weather").toString()
//
//        weatherLayout.addView(weatherView)

//        windTextView.text = jsonOBJ.getString("wind").toString()

    }
    private fun changeCamera(){
        getWeather()

        googleMap.addMarker(MarkerOptions().position(latlng).title(loc))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,10.0f))

    }
    private fun locateAddress(): Boolean{


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

            Log.i("Location Service","checking location permissions")
            if (context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } == PackageManager.PERMISSION_GRANTED &&
                context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED
            ) {

                Log.i("Location Service","Permission Granted")
                getDeviceLocation()
            }
            else{
                requestLocationPermissions()
                isLocationEnabled()

                val frag=HomeFragment.newInstance()
                (activity as MainActivity).replaceFragment(frag,HomeFragment.TAG)

            }

            return false
        }
        else{
            googleMap.isMyLocationEnabled = false;
            latlng  = LatLng(addressList[0].latitude,addressList[0].longitude)
            changeCamera()
        }


        return true
    }


    private fun checkLocationPermissions(): Boolean {
        if (context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } == PackageManager.PERMISSION_GRANTED &&
            context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }
    private fun requestLocationPermissions(){
        when {
            activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION).toString()
                )
            } == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(activity,"You have Already Granted this", Toast.LENGTH_SHORT).show()
            }
            activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION).toString()) }!! -> {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Location Permission needed")
                    .setMessage("This Permission is needed to access current api")
                    .setPositiveButton("ok", DialogInterface.OnClickListener{ dialogInterface: DialogInterface, i: Int ->
                        fun onClick(dialog: DialogInterface, which: Int){
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),locationCode)
                        }
                    })
                    .setNegativeButton("cancel", DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                        fun onClick(dialog: DialogInterface, which: Int){
                            Toast.makeText(activity,"Location Permission is required",Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    })
                    .create().show()
            }
            else -> {
                ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),locationCode)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == locationCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                locateAddress()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        Log.i("Location Service","Getting Device Location")

        if(checkLocationPermissions()) {
            googleMap.isMyLocationEnabled = true;

        }

        val locationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        locationClient.lastLocation
            .addOnSuccessListener { location -> // GPS location can be null if GPS is switched off
                location?.let {
                    //success
                    if(location !=null){

                        try{
                            Log.i("Location Service","configuring geocode: latlong -" +
                                    "${location.latitude} , ${location.longitude}")
                            val geocoder =  Geocoder(activity,Locale.getDefault())
                            val address: List<Address> = geocoder.getFromLocation(location.latitude,location.longitude,1)
                            latlng = LatLng(address[0].latitude,address[0].longitude)

                            googleMap.clear()
                            changeCamera()


                        } catch (e: Exception){
                            e.message?.let { it1 -> Log.i("Location Service", it1) }
                        }

                    } else{
                        Log.i("Location Service","geocode location is null")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.d("MapDemoActivity", "Error trying to get last GPS location")
                e.printStackTrace()
            }

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
        val TAG: String = MapFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(location: String) = MapFragment(location)
    }



}