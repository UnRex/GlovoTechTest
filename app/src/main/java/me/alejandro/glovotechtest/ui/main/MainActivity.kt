package me.alejandro.glovotechtest.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import me.alejandro.glovotechtest.R
import me.alejandro.glovotechtest.model.CityDetailed
import me.alejandro.glovotechtest.ui.base.BaseActivity
import me.alejandro.glovotechtest.ui.custom.CityDialog
import me.alejandro.glovotechtest.util.Constants
import me.alejandro.glovotechtest.util.Constants.Companion.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import java.io.IOException
import java.util.*


class MainActivity : BaseActivity<MainContract.View, MainPresenter>(), MainContract.View, OnMapReadyCallback {

    override var presenter: MainPresenter = MainPresenter()

    private lateinit var mMap: GoogleMap

    private lateinit var infoLayout: RelativeLayout

    private lateinit var noInfo: TextView
    private lateinit var city: TextView
    private lateinit var country: TextView
    private lateinit var enabled: TextView
    private lateinit var busy: TextView
    var allMarkersShown = false

    var firstRun = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        infoLayout = findViewById(R.id.info_layout)

        noInfo = findViewById(R.id.no_info)
        city = findViewById(R.id.info_city_value)
        country = findViewById(R.id.info_country_value)
        enabled = findViewById(R.id.info_enabled_value)
        busy = findViewById(R.id.info_busy_value)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnCameraIdleListener {
            if (mMap.cameraPosition.zoom < Constants.ZOOM_LEVEL_CITY) {
                showLoading()
                showAllMarkers()
            }

        }

        mMap.setOnMarkerClickListener { marker ->
            presenter.getDetailedCityInfo(presenter.getCityFromMarker(marker.id))
            true
        }

        presenter.checkData(this@MainActivity)

        askForLocationPermission()
    }

    private fun showAllMarkers() {

        if (!allMarkersShown) {
            allMarkersShown = true

            presenter.getCities()?.forEach {
                setPositionOnMapByCityName(it.name, false)
            }

            hideLoading()
        }
    }

    override fun storeResponse(response: String, key: String) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString(key, response)
            .apply()
    }

    private fun askForLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )

            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
        } else {
            requestLocation()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    requestLocation()

                } else {
                    showCityDialog()
                }
                return
            }
        }
    }

    private fun showCityDialog() {

        val cityDialog = CityDialog()
        val fm = supportFragmentManager

        cityDialog.show(fm, "city")

    }

    private fun requestLocation() {
        showLoading()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000L, 10F, object : android.location.LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val longitude = location.longitude

                        val latitude = location.latitude

                        var cityName = ""
                        val gcd = Geocoder(baseContext, Locale.getDefault())
                        val addresses: List<Address>
                        try {
                            addresses = gcd.getFromLocation(
                                location.latitude,
                                location.longitude, 1
                            )
                            if (addresses.isNotEmpty()) {
                                System.out.println(addresses[0].locality)
                                cityName = addresses[0].locality
                            }

                            presenter.checkIfCityExists(cityName).let {
                                if (it != null && it) {
                                    val position = LatLng(latitude, longitude)

                                    setPositionOnMapByLatLn(position, presenter.getCityCodeFromCityName(cityName))

                                    hideLoading()
                                } else {
                                    hideLoading()
                                    showCityDialog()
                                }
                            }

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                    override fun onProviderEnabled(provider: String?) {}

                    override fun onProviderDisabled(provider: String?) {}

                }
            )
        }
    }

    private fun setPositionOnMapByLatLn(
        position: LatLng,
        cityCode: String?,
        zoomLevel: Float = Constants.ZOOM_LEVEL_CITY,
        performZoom: Boolean = true
    ) {

        val marker = mMap.addMarker(MarkerOptions().position(position))
        var city = ""
        if (!cityCode.isNullOrBlank()) {
            city = cityCode
        }

        presenter.addMarker(marker.id, city)

        if (performZoom) {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 2000, null)

            val cameraPosition = CameraPosition.Builder()
                .target(position)
                .zoom(zoomLevel)
                .build()

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

    }

    fun setPositionOnMapByCityName(cityName: String?, performZoom: Boolean = true) {
        cityName.let {
            if (Geocoder.isPresent()) {
                try {
                    val gc = Geocoder(this)
                    val addresses = gc.getFromLocationName(cityName, 5)

                    val ll = ArrayList<LatLng>(addresses.size)
                    for (a in addresses) {
                        if (a.hasLatitude() && a.hasLongitude()) {
                            ll.add(LatLng(a.latitude, a.longitude))
                        }
                    }

                    if (ll.isNotEmpty()) {
                        var city = ""
                        if (!cityName.isNullOrBlank()) {
                            city = cityName
                        }
                        setPositionOnMapByLatLn(
                            ll[0],
                            performZoom = performZoom,
                            cityCode = presenter.getCityCodeFromCityName(city)
                        )
                    }
                } catch (e: IOException) {
                    // handle the exception
                }

            }
        }
    }

    override fun fillCityInfo(city: CityDetailed) {
        if (noInfo.visibility == View.VISIBLE) {
            noInfo.visibility = View.GONE
            infoLayout.visibility = View.VISIBLE
        }

        this.city.text = city.name
        this.country.text = presenter.getCountryByCode(city.country_code)
        this.enabled.text = if (city.enabled) "Yes" else "No"
        this.busy.text = if (city.busy) "Yes" else "No"

        setPositionOnMapByCityName(city.name)
    }
}
