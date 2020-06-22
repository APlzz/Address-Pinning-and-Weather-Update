package com.example.simpleweatherapp.ui.home

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.example.simpleweatherapp.MainActivity
import com.example.simpleweatherapp.R
import com.example.simpleweatherapp.ui.map.MapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment(){

    private val internetTag = "Internet Access"
    private val locationTag = "Location Access"
    private val internetRequestCode = 1
    private val locationRequestCode = 2
    private lateinit var homeViewModel: HomeViewModel
    private val latlng = LatLng(13.621775, 123.194824)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)


        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val textEditBarangay: EditText = root.findViewById(R.id.editTextBarangay)
        val textEditCity: EditText = root.findViewById(R.id.editTextCity)
        val textEditProvince: EditText = root.findViewById(R.id.editTextProvince)
        val submitAddressButton: Button = root.findViewById(R.id.submitAddressButton)

        fun checkFields(): Boolean {
            if(textEditBarangay.text.toString() == "") {
                textEditBarangay.requestFocus()
                textEditBarangay.error = "Field Cannot be empty"

                return false
            }
            if(textEditCity.text.toString() == "") {
                textEditCity.requestFocus()
                textEditCity.error = "Field Cannot be empty"

                return false
            }
            if(textEditProvince.text.toString() == "") {
                textEditProvince.requestFocus()
                textEditProvince.error = "Field Cannot be empty"

                return false
            }

            return true
        }




        submitAddressButton.setOnClickListener {

            if(activity?.let { it1 ->
                    ContextCompat.checkSelfPermission(
                        it1,
                        android.Manifest.permission.INTERNET)} == PackageManager.PERMISSION_GRANTED){

                // call api
                if(checkFields()) {
                    Log.i("geocoder","locating address")

                    val frag=MapFragment.newInstance("${editTextBarangay.text.toString()},"+
                    "${editTextCity.text.toString()},"+
                    editTextProvince.text.toString())
                    (activity as MainActivity).replaceFragment(frag,MapFragment.TAG)
                }
            } else{
                requestInternetPermission()
            }
        }


        return root
    }


    private fun requestInternetPermission(){
        when {
            activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.INTERNET)
            } == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(activity,"You have Already Granted this",Toast.LENGTH_SHORT).show()
            }
            activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it,android.Manifest.permission.INTERNET) }!! -> {
                AlertDialog.Builder(activity!!)
                    .setTitle("Internet Permission needed")
                    .setMessage("This Permission is needed to access the weather api")
                    .setPositiveButton("ok", DialogInterface.OnClickListener{ dialogInterface: DialogInterface, i: Int ->
                        fun onClick(dialog: DialogInterface, which: Int){
                            ActivityCompat.requestPermissions(
                                activity!!,
                                arrayOf(android.Manifest.permission.INTERNET),internetRequestCode)
                        }
                    })
                    .setNegativeButton("cancel", DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                        fun onClick(dialog: DialogInterface, which: Int){
                            dialog.dismiss()
                        }
                    })
                    .create().show()
            }
            else -> {
                ActivityCompat.requestPermissions(activity!!,arrayOf(android.Manifest.permission.INTERNET),internetRequestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == internetRequestCode){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(activity,"Granted",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(activity,"Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        val TAG = HomeFragment::class.java.simpleName
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

}