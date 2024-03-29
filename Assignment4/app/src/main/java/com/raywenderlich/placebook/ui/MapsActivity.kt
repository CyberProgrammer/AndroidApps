package com.raywenderlich.placebook.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mapsViewModel by viewModels<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupPlacesClient()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setupMapListeners()
        createBookmarkMarkerObserver()
        getCurrentLocation()
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId

        val placeFields = listOf(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG)

        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode)
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.photoMetadatas?.get(0)
        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))
            .build()
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
            val bitmap = fetchPhotoResponse.bitmap
            displayPoiDisplayStep(place, bitmap)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                Log.e(TAG, "Place not found: " + exception.message + ", statusCode: " + statusCode)
            }
        }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val marker = map.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber)
        )
        marker?.tag = PlaceInfo(place, photo)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun handleInfoWindowClick(marker: Marker) {
        val placeInfo = (marker.tag as PlaceInfo)
        if (placeInfo.place != null) {
            GlobalScope.launch {
                mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
            }
        }
        marker.remove()
    }

    private fun createBookmarkMarkerObserver() {
        mapsViewModel.getBookmarkMarkerViews()?.observe(
            this, {

                map.clear()

                it?.let {
                    displayAllBookmarks(it)
                }
            })
    }

    private fun displayAllBookmarks(bookmarks: List<MapsViewModel.BookmarkMarkerView>) {
        bookmarks.forEach { addPlaceMarker(it) }
    }

    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkMarkerView): Marker? {
        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_AZURE))
            .alpha(0.8f))
        if (marker != null) {
            marker.tag = bookmark
        }
        return marker
    }



    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION)
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}