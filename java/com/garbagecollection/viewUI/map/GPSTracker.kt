package com.garbagecollection.viewUI.map

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class GPSTracker(private val mActivity: Activity?) : Service(), LocationListener {
    // flag for GPS Status

    var isGPSEnabled = false

    // flag for network status
    var isNetworkEnabled = false
    var canGetLocation = false
    private var locationsSaved: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0

    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null
    init {
        startTracking()
    }
    fun startTracking() {
        getLocation()
    }

    private fun getLocation(): Location? {
        try {
            locationManager = mActivity?.getSystemService(LOCATION_SERVICE) as LocationManager

            // getting GPS status
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(applicationContext,"GPS and network not enabled",Toast.LENGTH_SHORT).show()
                // no network provider is enabled
            } else {
                canGetLocation = true

                // First get location from Network Provider
                if (isNetworkEnabled) {
                    try {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 102)
                            return locationsSaved
                        }
                    } catch (e: Exception) {
                        e.message
                    }
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                    if (locationManager != null) {
                        locationsSaved = locationManager!!
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        updateGPSCoordinates()
                    }
                }
                if (isGPSEnabled) {
                    if (locationsSaved == null) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        if (locationManager != null) {
                            locationsSaved = locationManager!!
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            updateGPSCoordinates()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // e.printStackTrace();
            Log.e("Error : Location",
                "Impossible to connect to LocationManager", e)
        }
        return locationsSaved
    }

    fun updateGPSCoordinates() {
        if (locationsSaved != null) {
            latitude = locationsSaved!!.latitude
            longitude = locationsSaved!!.longitude
            //   Toast.makeText(mActivity, "lat:-" + location.getLatitude() + "\nlng:-" + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
            locationManager = null
        }
    }

    /**
     * Function to get latitude
     */
    fun getLatitude(): Double {
        if (locationsSaved != null) {
            latitude = locationsSaved!!.latitude
        }
        return latitude
    }

    /**
     * Function to get longitude
     */
    fun getLongitude(): Double {
        if (locationsSaved != null) {
            longitude = locationsSaved!!.longitude
        }
        return longitude
    }

    /**
     * Function to check GPS/wifi enabled
     */
    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    /**
     * Function to show settings alert dialog
     */
    fun showSettingsAlert() {
        if (mActivity == null || mActivity.isFinishing) {
            return
        }
       /* mActivity.runOnUiThread(Runnable {
            val alertDialog = AlertDialog.Builder(
                mActivity)

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings")

            // Setting Dialog Message
            alertDialog
                .setMessage("Your GPS is disabled, Enable GPS in settings or continue with approximate location")

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings"
            ) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mActivity.startActivity(intent)
            }

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel"
            ) { dialog, which ->
                // UserSerch doSerch = new UserSerch();
                // doSerch.doserchOn();
                // dialog.cancel();
            }

            // Showing Alert Message
            alertDialog.create().show()
        })*/
    }

    /**
     * Get list of address by latitude and longitude
     *
     * @return null or List<Address>
    </Address> */
    fun getGeocoderAddress(context: Context?): List<Address>? {
        if (locationsSaved != null) {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            try {
                return geocoder.getFromLocation(latitude,
                    longitude, 1)
            } catch (e: IOException) {
                // e.printStackTrace();
                Log.e("Error : Geocoder", "Impossible to connect to Geocoder",
                    e)
            }
        }
        return null
    }


    /**
     * Try to get AddressLine
     *
     * @return null or addressLine
     */
    fun getAddressLine(context: Context?): String? {
        val addresses = getGeocoderAddress(context)
        return if (addresses != null && addresses.size > 0) {
            val address = addresses[0]
            address.getAddressLine(0)
        } else {
            null
        }
    }

    /**
     * Try to get Locality
     *
     * @return null or locality
     */
    fun getLocality(context: Context?): String? {
        val addresses = getGeocoderAddress(context)
        return if (addresses != null && addresses.size > 0) {
            val address = addresses[0]
            address.locality
        } else {
            null
        }
    }

    /**
     * Try to get Postal Code
     *
     * @return null or postalCode
     */
    fun getPostalCode(context: Context?): String? {
        val addresses = getGeocoderAddress(context)
        return if (addresses != null && addresses.size > 0) {
            val address = addresses[0]
            address.postalCode
        } else {
            null
        }
    }

    /**
     * Try to get CountryName
     *
     * @return null or postalCode
     */
    fun getCountryName(context: Context?): String? {
        val addresses = getGeocoderAddress(context)
        return if (addresses != null && addresses.size > 0) {
            val address = addresses[0]
            address.countryName
        } else {
            null
        }
    }

    lateinit var listner: LocationCall

    fun updateLocationToserver(listner: LocationCall) {
        this.listner=listner
    }

    override fun onLocationChanged(location: Location) {
        this.locationsSaved = location
        if (this::listner.isInitialized) {
            if (locationsSaved != null) {
                listner.onLocationCallBack(locationsSaved!!)
            }
            /*Handler(Looper.getMainLooper()).postDelayed({

            }
        }, 10000)*/
        }
    }

    override fun onProviderDisabled(provider: String) {
        showSettingsAlert()
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private val TAG = GPSTracker::class.java.simpleName

        // The minimum distance to change updates in metters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 1 // 10

        // metters
        // The minimum time beetwen updates in milliseconds
        private const val MIN_TIME_BW_UPDATES: Long = 1000 // * 60 * 1; // 1 minute
    }



}

    interface LocationCall {
        fun onLocationCallBack(
            location: Location
        )

}




