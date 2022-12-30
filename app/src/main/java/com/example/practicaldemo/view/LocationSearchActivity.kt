package com.example.practicaldemo.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.practicaldemo.*
import com.example.practicaldemo.directionhelpers.FetchURL
import com.example.practicaldemo.directionhelpers.TaskLoadedCallback
import com.example.practicaldemo.model.AddressesList
import com.example.practicaldemo.utils.GpsTracker
import com.example.practicaldemo.utils.Utils
import com.example.practicaldemo.viewmodel.DataViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_location_search.*
import java.io.IOException
import java.util.*


class LocationSearchActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener, View.OnClickListener, TaskLoadedCallback {

    private var tempAddress = ""
    private var cityName = ""
    private var tempLongitude = 0.0
    private var tempLatitude = 0.0
    var geocoder: Geocoder? = null
    private var gpsTracker: GpsTracker? = null
    var addresses: List<Address>? = null
    private var mMap: GoogleMap? = null
    private var marker: Marker? = null
    val AUTOCOMPLETE_REQUEST_CODE = 101
    val LOCATION_REQ_CODE = 101
    var latitude = 0.0
    var longitude = 0.0
    var addressId = 0L
    var isEdited = false
    var addressList: ArrayList<AddressesList> = ArrayList<AddressesList>()
    var markerList: ArrayList<LatLng> = ArrayList<LatLng>()
    private var place1: MarkerOptions? = null
    private var place2: MarkerOptions? = null
    private var currentPolyline: Polyline? = null

    companion object {
        lateinit var datarecordViewModel: DataViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_search)
        supportActionBar!!.title = getString(R.string.location_search)
        datarecordViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
        markerList.clear()
        currentLocation.text = getString(R.string.save)
        init()

        intent?.let {
            val bundle = intent.extras
            if (bundle?.getSerializable("AddressList") != null) {
                addressList = bundle.getSerializable("AddressList") as ArrayList<AddressesList>
                addressId = bundle.getLong("AddressId")
                var editLocation = bundle.getBoolean("isEdit")
                for (i in 0 until addressList.size) {
                    if (addressId == addressList[i].id) {
                        edtSearchLocation.text = addressList[i].address
                        tempLatitude = addressList[i].latitude.toDouble()
                        tempLongitude = addressList[i].longitude.toDouble()
                        latitude = addressList[i].latitude.toDouble()
                        longitude = addressList[i].longitude.toDouble()
                        cityName = addressList[i].cityName
                        tempAddress = addressList[i].address
                        if (editLocation) {
                            isEdited = true
                            currentLocation.text = getString(R.string.update)
                            setCurrentLocationOnMap()
                        }else{
                            val myCurrentLocation = LatLng(Utils.currentLatitude, Utils.currentLongitude)
                            val destinationLocation = LatLng(tempLatitude, tempLongitude)
                            markerList.add(myCurrentLocation)
                            markerList.add(destinationLocation)
                            markerSet(
                                markerList
                            )
                            drawLine(myCurrentLocation, destinationLocation)
                        }

                    }
                }
            }
        }


    }

    private fun init() {
        currentLocation.setOnClickListener(this)
        edtSearchLocation.setOnClickListener(this)
        savedaddressList.setOnClickListener(this)

        val apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    fun onSearchCalled() {
        val fields =
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.currentLocation -> {
                saveAddress()
            }
            R.id.savedaddressList -> {
                var intent = Intent(this, LocationSourceActivity::class.java)
                startActivity(intent)
            }
            R.id.edtSearchLocation -> onSearchCalled()
        }
    }

    private fun saveAddress() {
        if (!edtSearchLocation.text.isNullOrEmpty()) {
            var addressdata = AddressesList(
                0,
                tempLatitude.toString(),
                tempLongitude.toString(),
                cityName,
                tempAddress
            )
            if (isEdited) {
                isEdited = false
                Log.d("==>TAG : ", "update 123: ")
                datarecordViewModel.updateData(
                    tempLatitude.toString(),
                    tempLongitude.toString(),
                    cityName,
                    tempAddress, addressId
                )
            } else {
                datarecordViewModel.insert(addressdata)
            }
            var intent = Intent(this, LocationSourceActivity::class.java)
            startActivity(intent)
            currentLocation.text = "Save"
            edtSearchLocation.text = ""
        } else {
            Toast.makeText(this, "Search Locatiom", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {

                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    tempLatitude = place.latLng?.latitude!!
                    tempLongitude = place.latLng?.longitude!!
                    tempAddress = place.address.toString()
                    edtSearchLocation.text = place.address
                    markerMovement(LatLng(tempLatitude, tempLongitude))

                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Log.d(
                        "==TAG", status.statusMessage!!
                    )
                }
                RESULT_CANCELED -> {
                    Log.d(
                        "==TAG", "Result Cancel"
                    )
                }
            }
        }
    }

    private fun markerSet(latLng: ArrayList<LatLng>) {
        for (i in 0 until latLng.size) {
            mMap?.addMarker(MarkerOptions().position(latLng.get(i)).title("Marker"))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(18.0f))
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng.get(i)))
        }
//
    }

    fun getCurrentLocation() {

        try {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), LOCATION_REQ_CODE
                )
            } else {

                gpsTracker = GpsTracker(this)

                if (gpsTracker?.canGetLocation()!!) {
                    latitude = gpsTracker?.getLatitude()!!
                    longitude = gpsTracker?.getLongitude()!!
//                    Utils.currentLatitude = latitude
//                    Utils.currentLongitude = longitude
                    Log.d(
                        "==>TAG : ",
                        "Set current location : ${Utils.currentLatitude} - ${Utils.currentLongitude}"
                    )
                    geocoder = Geocoder(this, Locale.getDefault())
                    setCurrentLocationOnMap()

                    try {

                        addresses = geocoder?.getFromLocation(
                            latitude,
                            longitude,
                            1
                        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                        val address: String? = addresses?.get(0)
                            ?.getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        tempLatitude = latitude
                        tempLongitude = longitude

                        tempAddress = address.toString()
                        cityName = addresses?.get(0).toString()
//                        edtSearchLocation.text = address.toString()

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.d("==> TAG :", "Exception$e")
                    }

                } else {

                    gpsTracker?.showSettingsAlert()
                }
            }

        } catch (e: Exception) {

        }
    }

    private fun setCurrentLocationOnMap() {

        if (mMap != null) {

            mMap?.clear()

            val latLng = LatLng(latitude, longitude)
            marker = mMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
            )
            val location = CameraUpdateFactory.newLatLngZoom(
                latLng, 15f
            )
            mMap?.animateCamera(location)
            mMap?.uiSettings?.isZoomControlsEnabled = true

        }
    }


    override fun onResume() {
        super.onResume()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d("==> TAG :", "onRequestPermissionsResult" + requestCode)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // now, you have permission go ahead
            Log.d("==> TAG :", "Permission Granted")
            getCurrentLocation()

        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // now, user has denied permission (but not permanently!)
                Log.d("==> TAG :", "Permission Denied Temporary")
                permissionDeniedDialog(this)
            } else {

                // now, user has denied permission permanently!
                Log.d("==> TAG :", "Permission Denied Permanently")
                permissionDeniedDialog(this)
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        Log.e("location", "== $latitude")
        if (latitude == 0.0 && longitude == 0.0) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                markerMovement(LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0))
            }
        } else {
            val latLng = LatLng(latitude, longitude)
            markerMovement(latLng)
        }
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = true

        mMap?.setOnMarkerDragListener(this)
    }

    private fun markerMovement(latLng: LatLng) {
        if (marker == null) {
            marker = mMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
            )
        } else {
            marker?.position = latLng
        }
        setLocationOnMap(marker)
        val location = CameraUpdateFactory.newLatLngZoom(
            latLng, 15f
        )
        mMap?.animateCamera(location)
    }

    override fun onMarkerDragStart(p0: Marker) {

    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onMarkerDragEnd(p0: Marker) {
        setLocationOnMap(p0)
    }

    private fun setLocationOnMap(p0: Marker?) {
        val latLng = p0?.position
        val geocoder = Geocoder(this, Locale.getDefault())
        latLng?.let {
            try {
                val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)[0]
                if (address.maxAddressLineIndex >= 0) {
                    tempLatitude = latLng.latitude
                    tempLongitude = latLng.longitude
                    tempAddress = address.getAddressLine(0)
                    cityName = address.locality
//                    edtSearchLocation.text = address.getAddressLine(0)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

    }

    fun permissionDeniedDialog(context: Context) {

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle("Location Permission")
        var positiveText = "OK"
        builder.setPositiveButton(positiveText, null)
        builder.setMessage("You have previously declined this permission.You must approve this permission, In Permissions in the app settings on your device.")

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
    }


    private fun drawLine(sourceLocation: LatLng, destinationLocation: LatLng) {
        place1 = MarkerOptions().position(LatLng(sourceLocation.latitude, sourceLocation.longitude))
            .title("Destination")
        place2 = MarkerOptions().position(
            LatLng(
                destinationLocation.latitude,
                destinationLocation.longitude
            )
        ).title("Source")
        FetchURL(this).execute(
            getUrl(place1?.position!!, place2?.position!!, "Address"),
            "Address"
        )

    }


    private fun getUrl(origin: LatLng, dest: LatLng, directionMode: String): String? {
        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        // Mode
        val mode = "mode=$directionMode"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(
            R.string.api_key
        )
    }

    override fun onTaskDone(vararg values: Any?) {
        currentPolyline?.remove()
        currentPolyline = mMap?.addPolyline(values[0] as PolylineOptions)
    }

}