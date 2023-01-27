package com.example.wander

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.wander.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.*
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val TAG = MapsActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    mapFragment.getMapAsync(this)
                }
                else
                    Toast.makeText(this, R.string.permission_deny_msg, Toast.LENGTH_SHORT).show()
            }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker at your designated home location and move the camera
        val latitude = 16.780924639735776
        val longitude = 96.1712432894726
        val homeLatLng = LatLng(latitude, longitude)
        val zoomLevel = 18f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))

        // Add a ground overlay to specify an image label on top of a desired location
        val overlaySize = 100f
        val androidOverlay = GroundOverlayOptions()
        androidOverlay.image(BitmapDescriptorFactory.fromResource(R.drawable.android))
            .position(homeLatLng, overlaySize)
        map.addGroundOverlay(androidOverlay)

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Change the map type based on the user's selection
        when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                return true
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                return true
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                return true
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Allow users to add a marker using a long click
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            // Create additional texts such as longitude and latitude info of the marked location
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            )
        }
    }

    // Allow users to add a marker on POI an show its name in an info window
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
            )
            // Display POI's name in an info window
            poiMarker.showInfoWindow()
        }
    }

    // Create a custom map style
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the map with JSON object
            val success =
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success)
                Log.e(TAG, "Styling parsing failed!")
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: $e")
        }
    }

    // Check if location permission is granted
    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Enable location tracking
    private fun enableMyLocation() {
        if (isPermissionGranted())
            map.isMyLocationEnabled = true
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
}