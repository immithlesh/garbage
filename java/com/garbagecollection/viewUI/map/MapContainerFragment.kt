package com.garbagecollection.viewUI.map

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransitRoutingPreference
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.model.Route
import com.akexorcist.googledirection.util.DirectionConverter
import com.akexorcist.googledirection.util.execute
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindow.MarkerSpecification
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment
import com.garbagecollection.MyFirebaseMessagingService
import com.garbagecollection.R
import com.garbagecollection.app.GCBaseFragment
import com.garbagecollection.base.adapter.RecyclerCallback
import com.garbagecollection.base.adapter.RecyclerViewGenricAdapter
import com.garbagecollection.common.GCCommon
import com.garbagecollection.common.GCCommon.Companion.getMarkerIconFromDrawable
import com.garbagecollection.common.GCCommon.Companion.isNetworkAvailable
import com.garbagecollection.common.NotificationX
import com.garbagecollection.common.mapUtils.CustomSoredMarkers
import com.garbagecollection.common.mapUtils.MarketTagsUtils
import com.garbagecollection.databinding.CustomerItemViewBinding
import com.garbagecollection.databinding.FragmentMapContainerBinding
import com.garbagecollection.utils.PrefUtils
import com.garbagecollection.viewUI.GarbageCollectionPostDataNew
import com.garbagecollection.viewUI.map.room_db.GCDriverModel
import com.garbagecollection.viewUI.map.room_db.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.customers_view.view.*
import kotlinx.android.synthetic.main.fragment_map_container.*
import kotlinx.android.synthetic.main.view_notes_dialog.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.*
import kotlin.collections.ArrayList


class MapContainerFragment : GCBaseFragment<FragmentMapContainerBinding>(), OnMapReadyCallback,
    LocationCall {
    private var vm: MapViewModel? = null
    private lateinit var mMap: GoogleMap
    private lateinit var model: FetchMarkerModel
    private val waypoints: ArrayList<LatLng> = arrayListOf()
    private var Route: String? = null
    private var Day: String? = null
    private var DetailType: String? = null
    private var DayResponse: String? = null
    private var directionResponse: String? = null
    val addressTxt: String? = null
    private var infoWindowManager: InfoWindowManager? = null
    private var formWindow: InfoWindow? = null
    private var mAdapterCustomers: RecyclerViewGenricAdapter<Result, CustomerItemViewBinding>? =
        null
    var polylines: Polyline? = null
    private var list1: ArrayList<Result> = ArrayList()

    override fun getLayoutId(): Int {
        return R.layout.fragment_map_container
    }

    override fun getCurrentFragment(): Fragment {
        return this@MapContainerFragment
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    lateinit var gpsTracker: GPSTracker
    val mMarkerList = ArrayList<Marker>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MyFirebaseMessagingService.mNotifyCurrentFrag = this@MapContainerFragment
        vm = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        gpsTracker = GPSTracker(requireActivity())
        gpsTracker.updateLocationToserver(this)


        val bundle = this.arguments
        if (bundle != null) {
            Day = bundle.getString("day")
            Route = bundle.getString("route")
            DetailType = bundle.getString("detailType") ?: ""
            DayResponse = bundle.getString("DayResponse")
            directionResponse = bundle.getString("directionResponse")
            viewDataBinding!!.selectedDayTv.text = Day
            viewDataBinding!!.SelectedRouteTVId.text = Route
        }

        viewDataBinding!!.apply {

            customersBtn.setOnClickListener {
                viewCustomer()
                customersBtn.isClickable = false
            }

            backIcon.setOnClickListener {
                fragmentManager?.popBackStack()
            }

            val mapFragment: MapInfoWindowFragment =
                childFragmentManager.findFragmentById(R.id.map) as MapInfoWindowFragment
            infoWindowManager = mapFragment.infoWindowManager()
            infoWindowManager!!.setHideOnFling(true)
            mapFragment.getMapAsync(this@MapContainerFragment)
        }

    }

    private fun viewCustomer() {
        val dialog = Dialog(getContainerActivity())
        val v = layoutInflater.inflate(R.layout.customers_view, null)

        val activeLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        v.custumerListRec.layoutManager = activeLayoutManager


        mAdapterCustomers = RecyclerViewGenricAdapter(
            list1,
            R.layout.customer_item_view,
            object : RecyclerCallback<CustomerItemViewBinding, Result> {
                override fun bindData(
                    binder: CustomerItemViewBinding,
                    model: Result,
                    position: Int,
                    itemView: View?
                ) {
                    binder.apply {
                        customerListName.text = model.name
                        customerListAddress.text = model.location

                    }
                }

            })
        v.custumerListRec.adapter = mAdapterCustomers
        mAdapterCustomers?.notifyDataSetChanged()

        v.closeDialogId2CustView.setOnClickListener {
            dialog.dismiss()
            customersBtn.isClickable = true

        }
        val window = dialog.window
        window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        dialog.setCancelable(false)
        dialog.setContentView(v)
        dialog.show()

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
            if (!isNetworkAvailable(getContainerActivity())) {
                hideLoading()
                fromLocalDB()
            }
            infoWindowManager!!.onMapReady(googleMap)
            mMap.uiSettings.setAllGesturesEnabled(true)
            mMap.uiSettings.isZoomGesturesEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isScrollGesturesEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
            mMap.setMaxZoomPreference(17.0f)
            mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    getContainerActivity(),
                    R.raw.style_json
                )
            )
            mMap.isBuildingsEnabled = true
            mMap.isIndoorEnabled = true
            mMap.uiSettings.isIndoorLevelPickerEnabled = true

            if (ActivityCompat.checkSelfPermission(
                    getContainerActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    getContainerActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }


//            mMap.isMyLocationEnabled = true
            //      mMap.addTileOverlay(TileOverlayOptions().tileProvider(CustomMapTileProvider(resources.assets)))
            val upd =
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        gpsTracker.getLatitude(),
                        gpsTracker.getLongitude()
                    ), 15f
                )
            mMap.moveCamera(upd)


            getCustMarkerApi(
                DetailType!!,
                viewDataBinding!!.SelectedRouteTVId.text.toString(),
                viewDataBinding!!.selectedDayTv.text.toString(),
                gpsTracker.getLatitude(), gpsTracker.getLongitude()
            )

        }
    }


    fun getCustMarkerApi(
        type: String,
        route: String,
        selectedDay: String,
        latitude: Double,
        longitude: Double
    ) {
        list1.clear()
        disposable.add(
            getApiService()!!.getFilterMap(type, route, selectedDay, latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<FetchMarkerModel>() {
                    override fun onSuccess(model: FetchMarkerModel) {
                        this@MapContainerFragment.model = model

                        list1.addAll(model.data!!.results!!)
                        if (list1.size == 0) {
                            AlertDialog.Builder(requireActivity()).setTitle("Alert")
                                .setMessage("No Route Available.")
                                .setPositiveButton(
                                    "Ok"
                                ) { dialog, which ->
                                    fragmentManager?.popBackStack()
                                    dialog.dismiss()
                                }.create().show()
                        }
                        fromServer(model)
                        checkAllCustomerServed()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("logE", "" + e)
                    }
                })
        )
    }

    private fun setSubmittedValue(isSubmit: Boolean) {
        isSubmitted = flow {
            emit(isSubmit)
        }.flowOn(Dispatchers.Default)
    }

    fun fromServer(model: FetchMarkerModel) {

        gpsTracker.startTracking()
        list1.forEach {
            waypoints.add(LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble()))
        }

        if (list1.isNullOrEmpty()) {
            return
        }

        val filter = list1.filter { it.isGarbageSubmit }
        setSubmittedValue(!filter.isNullOrEmpty())
        drawRoute(waypoints, model)
    }



    fun drawRoute(waypoints: ArrayList<LatLng>, model: FetchMarkerModel) {
        try {
            GoogleDirection.withServerKey(getString(R.string.google_maps_key))
//            .from(LatLng(list1[0].latitude!!, list1[0].longitude!!))
                .from(LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()))
                .and(waypoints[1])
                .to(LatLng(list1[list1.size - 1].latitude!!, list1[list1.size - 1].longitude!!))
                .transportMode(TransportMode.DRIVING)
                .alternativeRoute(false)
                .optimizeWaypoints(true)
                .transitRoutingPreference(TransitRoutingPreference.LESS_WALKING)

                .execute(
                    onDirectionSuccess = { direction: Direction? ->
                        if (direction!!.isOK()) {
                            val outPutString: String = Gson().toJson(direction)
                            val beckendResponse: String = Gson().toJson(model)
                            val route = direction.routeList[0]
                            val legCount = route.legList.size
                            if (vm?.getupdateListener != null) {
                                vm?.getupdateListener?.observe(viewLifecycleOwner, Observer {
                                    if (it) {
                                        if (vm?.gcwebDriverData != null) {
                                            GlobalScope.launch(Dispatchers.IO) {
                                                vm?.updateRoute2(
                                                    beckendResponse,
                                                    outPutString,
                                                    vm?.gcwebDriverData?.id!!
                                                )
                                                this.cancel()
                                            }

                                        } else {

                                            vm?.insertMapDriver(
                                                GCDriverModel(
                                                    1,
                                                    Route!!,
                                                    directionResponse,
                                                    Day,
                                                    DayResponse,
                                                    outPutString,
                                                    beckendResponse,
                                                    DetailType,
                                                    vm?.gcwebDriverData?.name,
                                                    vm?.gcwebDriverData?.location,
                                                    vm?.gcwebDriverData?.notes,
                                                    vm?.gcwebDriverData?.pinColor
                                                )
                                            )
                                        }
                                    }

                                    vm?.getupdateListener?.removeObservers(viewLifecycleOwner)
                                })
                            }
                            GlobalScope.launch(Dispatchers.IO) {
                                vm?.getExistingDriver(
                                    1,
                                    Route!!,
                                    Day!!,
                                    DetailType!!,
                                    getContainerActivity()
                                )
                                this.cancel()
                            }

                            for (index in 0 until legCount) {
                                val leg = route.legList[index]
                                val stepList = leg.stepList
                               var polylineOptionList = DirectionConverter.createTransitPolyline(
                                    getContainerActivity(),
                                    stepList,
                                    5,
                                    context?.getColor(R.color.green2)!!,
                                    3,
                                    context?.getColor(R.color.btn_bg_color)!!
                                )

                                for (polylineOption in polylineOptionList!!) {
                                    polylines = mMap.addPolyline(polylineOption)
                                    polylines?.isGeodesic=true
                                    polylines!!.isClickable = false

                                }
                            }
                            list1.forEachIndexed { index, markerLoactionModel ->
                                val offsetX = resources.getDimension(R.dimen._1sdp).toInt()
                                val offsetY = resources.getDimension(R.dimen._25sdp).toInt()
                                val markerSpec = MarkerSpecification(offsetX, offsetY)

                                val markerOptions = MarkerOptions()
                                markerOptions.position(
                                    LatLng(
                                        markerLoactionModel.latitude!!,
                                        markerLoactionModel.longitude!!
                                    )
                                )

                                var drawableIcon: Drawable? = null
                                val marker = mMap.addMarker(markerOptions)
                                marker?.tag = Gson().toJson(markerLoactionModel)
                                Log.e("MyMarker", "$index")
                                Log.e("MyMarker >>>>", "${list1.size}")
                                if (list1.size != mMarkerList.size) {
                                    mMarkerList.add(marker!!)
                                }
                                val savedValue = PrefUtils.getSaveValue(
                                    requireActivity(), GCCommon.MY_MARKET_INFO
                                )
                                val obj = Gson().fromJson(
                                    savedValue,
                                    CustomSoredMarkers::class.java
                                )

                                if (markerLoactionModel.isNew && !this.checkIsSubmitted()) {
                                    drawableIcon = getDrawableRes(
                                        obj,
                                        markerLoactionModel,
                                        R.drawable.ic_marker_blue
                                    )
                                } else if (!markerLoactionModel.isNew && !this.checkIsSubmitted()) {
                                    drawableIcon = getDrawableRes(
                                        obj,
                                        markerLoactionModel,
                                        R.drawable.ic_marker_grenn
                                    )
                                } else {
                                    drawableIcon = ContextCompat.getDrawable(
                                        requireActivity(),
                                        R.drawable.ic_marker_red
                                    )!!
                                    DrawableCompat.setTint(
                                        drawableIcon,
                                        (Color.parseColor("#FF0000"))
                                    )
                                }

                                val markerIcon = getMarkerIconFromDrawable(drawableIcon)

                                marker?.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(markerIcon!!)
                                )

                                hideLoading()
                                Log.e("mMarkerList >>>>", "${mMarkerList.size}")
                                mMap.setOnMarkerClickListener {
                                    Log.e("MyMarker >>>>", "${mMarkerList.size}")
                                    val passingObj = it.tag
                                    Log.e("MyMarker Tag", "Old $passingObj")
                                    var infoWindow: InfoWindow? = null
                                    formWindow =
                                        InfoWindow(
                                            it,
                                            markerSpec,
                                            InfoWindowFragment(
                                                this@MapContainerFragment,
                                                it.tag,
                                                object : InfoWindowFragment.onDismissLister {
                                                    override fun onBottomInfoDismiss() {
                                                        infoWindowManager!!.toggle(
                                                            infoWindow!!,
                                                            true
                                                        )
                                                    }

                                                    override fun onSubmitListener(id: Int) {
                                                        Log.e(
                                                            "ididid",
                                                            "$id"
                                                        )
                                                        if (id != -1) {
                                                            val drawableIcon =
                                                                ContextCompat.getDrawable(
                                                                    requireActivity(),
                                                                    R.drawable.ic_marker_grenn
                                                                )!!
                                                            DrawableCompat.setTint(
                                                                drawableIcon,
                                                                (Color.parseColor("#FF0000"))
                                                            )
                                                            val markerIcon =
                                                                getMarkerIconFromDrawable(
                                                                    drawableIcon
                                                                )
                                                            mMarkerList.forEachIndexed { index, marker ->
                                                                Log.e(
                                                                    "mMarkerList",
                                                                    "$index , ${it.tag}"
                                                                )
                                                            }
                                                            val mMarker = mMarkerList.find { it ->
                                                                it.tag == passingObj
                                                            }
                                                            Log.e(
                                                                "MyMarker Tag",
                                                                "New ${mMarker?.tag as String}"
                                                            )
                                                            Log.e(
                                                                "MyMarker Tag",
                                                                "${mMarkerList.size}"
                                                            )
                                                            mMarker?.setIcon(
                                                                BitmapDescriptorFactory.fromBitmap(
                                                                    markerIcon!!
                                                                )
                                                            )
                                                        }
                                                    }
                                                })
                                        )

                                    infoWindow = formWindow
                                    if (infoWindow != null) {
                                        infoWindowManager!!.toggle(infoWindow, true)
//                                        notifyLiveData.postValue(true)
                                        LocalBroadcastManager.getInstance(requireActivity())
                                            .sendBroadcast(
                                                Intent("call")
                                            )
                                    }
                                    return@setOnMarkerClickListener true
                                }
                            }
                            setCameraWithCoordinationBounds(route)

                        } else {
                            // Do something
                        }
                    },
                    onDirectionFailure = { t: Throwable ->
                    }
                )
        } catch (e: Exception) {
            print(e)
        }

    }

    private fun getDrawableRes(
        obj: CustomSoredMarkers?,
        markerLocationModel: Result,
        icMarkerOrange: Int
    ): Drawable {

        if (obj != null && !obj.data.isNullOrEmpty()) {
            val savedDataList = obj.data
            if (savedDataList.size > 0) {
                val idKeyExisted = savedDataList.find {
                    it.id == markerLocationModel.id
                }
                if (idKeyExisted != null) {
                    Log.e("getDrawableRes", "${markerLocationModel?.name}")
                    val drawableIcon = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_marker_red
                    )!!
                    DrawableCompat.setTint(
                        drawableIcon,
                        (Color.parseColor("#FF0000"))
                    )
                    return drawableIcon
                } else {
                    val drawableIcon = ContextCompat.getDrawable(
                        requireActivity(),
                        icMarkerOrange
                    )!!
                    return drawableIcon
                }
            }
        } else {
            val drawableIcon = ContextCompat.getDrawable(
                requireActivity(),
                icMarkerOrange
            )!!
            return drawableIcon
        }
        val drawableIcon = ContextCompat.getDrawable(
            requireActivity(),
            icMarkerOrange
        )!!
        return drawableIcon
    }

    fun fromLocalDB() {
        vm?.getFinalMapData(1, Day!!, Route!!, DetailType!!)?.observe(viewLifecycleOwner, Observer {

            if (it == null) {
                ExitAlert("No customers found.Please turn on your internet.", "Garbage Collection")

                return@Observer
            }

            val direction = Gson().fromJson<Direction>(it.polylineResponse, Direction::class.java)
            model =
                Gson().fromJson<FetchMarkerModel>(it.backendResponse, FetchMarkerModel::class.java)

            if (model != null) {
                list1.clear()
                list1.addAll(model.data!!.results!!)
                checkAllCustomerServed()
            }
            val filter = model.data?.results?.filter { it.isGarbageSubmit }
            setSubmittedValue(!filter.isNullOrEmpty())
            if (direction!!.isOK()) {
                val route = direction.routeList[0]
                val legCount = route.legList.size
                for (index in 0 until legCount) {
                    val leg = route.legList[index]
                    val stepList = leg.stepList
                    val polylineOptionList = DirectionConverter.createTransitPolyline(
                        getContainerActivity(),
                        stepList,
                        5,
                        context?.getColor(R.color.green2)!!,
                        3,
                        context?.getColor(R.color.btn_bg_color)!!
                    )

                    for (polylineOption in polylineOptionList) {
                        polylines = mMap.addPolyline(polylineOption)
                        polylines!!.isClickable = false

                    }
                }
                list1.forEachIndexed { index, markerLoactionModel ->
                    val offsetX = resources.getDimension(R.dimen._1sdp).toInt()
                    val offsetY = resources.getDimension(R.dimen._25sdp).toInt()
                    val markerSpec = MarkerSpecification(offsetX, offsetY)

                    val markerOptions = MarkerOptions()
                    markerOptions.position(
                        LatLng(
                            markerLoactionModel.latitude!!,
                            markerLoactionModel.longitude!!
                        )
                    )

                    val marker = mMap.addMarker(markerOptions)
                    var drawableIcon: Drawable? = null
                    marker?.tag = Gson().toJson(markerLoactionModel)
                    Log.e("MyMarker", "$index")
                    Log.e("MyMarker >>>>", "${list1.size}")
                    if (list1.size != mMarkerList.size) {
                        mMarkerList.add(marker!!)
                    }
                    if (markerLoactionModel.isNew && !this.checkIsSubmitted()) {
                        drawableIcon = ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_marker_blue
                        )!!
                    } else if (!markerLoactionModel.isNew && !this.checkIsSubmitted()) {
                        drawableIcon = ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_marker_grenn
                        )!!
                    } else {
                        drawableIcon = ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_marker_red
                        )!!
                        DrawableCompat.setTint(
                            drawableIcon,
                            (Color.parseColor("#FF0000"))
                        )
                    }

                    val savedValue = PrefUtils.getSaveValue(
                        requireActivity(), GCCommon.MY_MARKET_INFO
                    )

                    if (!TextUtils.isEmpty(savedValue)) {
                        val obj = Gson().fromJson(
                            savedValue,
                            CustomSoredMarkers::class.java
                        )
                        val savedDataList = obj.data
                        if (savedDataList.size > 0) {
                            val idKeyExisted = savedDataList.find {
                                it.id == markerLoactionModel.id
                            }
                            Log.d("idkey1","${idKeyExisted}")
                            Log.d("idkey2","${savedDataList.size}")
                            Log.d("idkey3","${savedDataList}")

                            if (idKeyExisted != null) {
                                DrawableCompat.setTint(
                                    drawableIcon,
                                    (Color.parseColor("#FF0000"))
                                )
                            }

                        }

                    }

                    val markerIcon = getMarkerIconFromDrawable(drawableIcon)

                    marker?.setIcon(
                        BitmapDescriptorFactory.fromBitmap(markerIcon!!)
                    )

                    hideLoading()
                    Log.e("mMarkerList >>>>", "${mMarkerList.size}")
                    mMap.setOnMarkerClickListener {
                        Log.e("MyMarker >>>>", "${mMarkerList.size}")
                        val passingObj = it.tag
                        Log.e("MyMarker Tag", "Old $passingObj")
                        var infoWindow: InfoWindow? = null
                        formWindow =
                            InfoWindow(
                                it,
                                markerSpec,
                                InfoWindowFragment(
                                    this@MapContainerFragment,
                                    it.tag,
                                    object : InfoWindowFragment.onDismissLister {
                                        override fun onBottomInfoDismiss() {
                                            infoWindowManager!!.toggle(
                                                infoWindow!!,
                                                true
                                            )
                                        }

                                        override fun onSubmitListener(id: Int) {
                                            if (id != -1) {
                                                val drawableIcon =
                                                    ContextCompat.getDrawable(
                                                        requireActivity(),
                                                        R.drawable.ic_marker_blue
                                                    )!!
                                                DrawableCompat.setTint(
                                                    drawableIcon,
                                                    (Color.parseColor("#FF0000"))
                                                )
                                                val markerIcon =
                                                    getMarkerIconFromDrawable(
                                                        drawableIcon
                                                    )
                                                mMarkerList.forEachIndexed { index, marker ->
                                                    Log.e(
                                                        "mMarkerList",
                                                        "$index , ${it.tag}"
                                                    )
                                                }
                                                val mMarker = mMarkerList.find { it ->
                                                    it.tag == passingObj
                                                }
                                                Log.e(
                                                    "MyMarker Tag",
                                                    "New ${mMarker?.tag as String}"
                                                )
                                                Log.e(
                                                    "MyMarker Tag",
                                                    "${mMarkerList.size}"
                                                )
                                                mMarker.setIcon(
                                                    BitmapDescriptorFactory.fromBitmap(
                                                        markerIcon!!
                                                    )
                                                )
                                            }
                                        }

                                    })
                            )

                        infoWindow = formWindow
                        if (infoWindow != null) {
                            infoWindowManager!!.toggle(infoWindow, true)
//                                        notifyLiveData.postValue(true)
                            LocalBroadcastManager.getInstance(requireActivity())
                                .sendBroadcast(
                                    Intent("call")
                                )
                        }
                        return@setOnMarkerClickListener true
                    }
                }

                setCameraWithCoordinationBounds(route)

            }
        })
    }

    private lateinit var isSubmitted: Flow<Boolean>

    fun checkIsSubmitted(): Boolean {
        var returnType = false
        runBlocking {
            isSubmitted.collect { isSubmitted ->
                returnType = isSubmitted
            }
        }
        return returnType
    }

    var notifyLiveData = MutableLiveData<Boolean>()

    private fun setCameraWithCoordinationBounds(route: Route) {
        val southwest = route.bound.southwestCoordination.coordination
        val northeast = route.bound.northeastCoordination.coordination
        val bounds = LatLngBounds(southwest, northeast)
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        mMap.animateCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    gpsTracker.getLatitude(),
                    gpsTracker.getLongitude()
                )
            )
        )
        mMap.setLatLngBoundsForCameraTarget(bounds)

    }


    private fun updateCameraBearing(googleMap: GoogleMap?, bearing: Float,latlon:LatLng) {
        if (googleMap == null) return
        val camPos = CameraPosition
            .builder(googleMap.cameraPosition)
            .target(latlon)
            .bearing(bearing)
            .tilt(50f)
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
    }

    var mPositionMarker: Marker? = null
    override fun onLocationCallBack(location: Location) {

        if (mPositionMarker == null) {
            mPositionMarker = mMap.addMarker(
                MarkerOptions()
                    .flat(true)
                    .icon(
                        BitmapDescriptorFactory
                            .fromResource(R.drawable.car50)
                    )
                    .anchor(0.5f, 0.5f)
                    .position(
                        LatLng(
                            location.latitude, location
                                .longitude
                        )
                    )
            )
        }
        animateMarker(mPositionMarker!!, location)
        updateCameraBearing(mMap, location.bearing,LatLng(location.latitude,location.longitude))
    }

    fun animateMarker(marker: Marker, location: Location) {
        val handler = Handler()
        val start: Long = SystemClock.uptimeMillis()
        val startLatLng = marker.position
        val startRotation = marker.rotation.toDouble()
        val duration: Long = 500
        val interpolator: Interpolator = LinearInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed: Long = SystemClock.uptimeMillis() - start
                val t: Float = interpolator.getInterpolation(
                    elapsed.toFloat() / duration
                )
                val lng = t * location.longitude + (1 - t) * startLatLng.longitude
                val lat = t * location.latitude + (1 - t) * startLatLng.latitude
                val rotation = (t * location.bearing + (1 - t) * startRotation).toFloat()
                marker.position = LatLng(lat, lng)
                marker.rotation = rotation
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    val garbageCollectedModel = ArrayList<GarbageCollectionPostDataNew>()

    fun checkAllCustomerServed() {
        viewDataBinding?.btnFinalSubmit?.visibility = View.GONE

        val savedValue = PrefUtils.getSaveValue(
            getContainerActivity(), GCCommon.MY_MARKET_INFO
        )

        if (TextUtils.isEmpty(savedValue)) {
            return
        }
        val obj = Gson().fromJson(savedValue, CustomSoredMarkers::class.java)
        val savedDataList = obj.data

        if (!this::model.isInitialized) {
            return
        }

        if (model.data?.results?.size != savedDataList.size) {
            return
        }

        viewDataBinding?.btnFinalSubmit?.visibility = View.VISIBLE
        finalSubmitToServer()
    }

    fun finalSubmitToServer() {
        viewDataBinding?.btnFinalSubmit?.setOnClickListener {
            val savedValue = PrefUtils.getSaveValue(
                requireActivity(), GCCommon.MY_MARKET_INFO
            )
            val obj = Gson().fromJson(savedValue, CustomSoredMarkers::class.java)
            val savedDataList = obj.data
            if (TextUtils.isEmpty(savedValue)) {
                showToast("Don't have any record yet")
                return@setOnClickListener
            }

            savedDataList.forEach { markerData ->
                val mList = markerData.data
                mList.forEach {
                    val selectedTag = getTagsforServer(it.tag)

                    garbageCollectedModel.add(
                        GarbageCollectionPostDataNew(
                            selectedTag,
                            markerData.id,
                            if (selectedTag == "NO_GARBAGE") 0 else it.selectedPos,
                            it.notes,
                            DetailType, Day
                        )
                    )
                }
            }

            if (isNetworkAvailable(getContainerActivity())) {
                disposable.add(
                    getApiService()!!.apiPostCollectedData(garbageCollectedModel)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<Any>() {

                            override fun onSuccess(model: Any) {
                                showToast("Submit successfully")
                                PrefUtils.clear(
                                    requireActivity(), GCCommon.MY_MARKET_INFO
                                )

                                checkAllCustomerServed()
                                setSubmittedValue(true)
                            }

                            override fun onError(e: Throwable) {
                            }
                        })
                )
            } else {
                showToast("please turn on internet to save collected garbage")
            }

        }
    }

    fun updateFrag(payload: NotificationX?) {
        getContainerActivity().runOnUiThread {
            val id = payload?.tag?.toInt()

            /**
             * Check in shared if value is stored
             **/
            val savedValue = PrefUtils.getSaveValue(
                requireActivity(), GCCommon.MY_MARKET_INFO
            )
            val obj = Gson().fromJson(
                savedValue,
                CustomSoredMarkers::class.java
            )
            if (obj != null && !obj.data.isNullOrEmpty()) {
                val isKeyIdExist = obj.data.find {
                    id == it.id
                }
                if (isKeyIdExist != null) {
                    for (i in list1.indices) {
                        if (isKeyIdExist.id == list1[i].id) {
                            val data = com.garbagecollection.viewUI.map.Note(
                                "",
                                list1[i].id,
                                payload!!.notes
                            )
                            list1[i].notes?.add(data)
                            mAdapterCustomers?.updateAdapterList(list1)
                            break
                        }
                    }
                } else {
                    updateMarker(id, payload)
                }
            } else {
                updateMarker(id, payload)
            }


        }
    }

    private fun updateMarker(id: Int?, payload: NotificationX?) {
        for (i in list1.indices) {
            val listId = list1[i].id
            if (list1[i].isNew && !this.checkIsSubmitted()) {
                if (listId == id) {
                    val drawableIcon =
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_marker_blue_with_note
                        )!!

                    val markerIcon =
                        getMarkerIconFromDrawable(
                            drawableIcon
                        )

                    val mMarker = mMarkerList.find { it ->
                        it.tag == Gson().toJson(list1[i])
                    }

                    mMarker?.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            markerIcon!!
                        )
                    )
                    val data = Note(
                        "", listId, payload!!.notes
                    )
                    list1[i].notes?.add(data)
                }
            } else if (!list1[i].isNew && !this.checkIsSubmitted()) {
                if (listId == id) {
                    val drawableIcon =
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_marker_green_with_notes
                        )!!
                    val markerIcon =
                        getMarkerIconFromDrawable(
                            drawableIcon
                        )

                    val mMarker = mMarkerList.find { it ->
                        it.tag == Gson().toJson(list1[i])
                    }

                    mMarker?.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            markerIcon!!
                        )
                    )
                    val data = Note("", listId, payload!!.notes)
                    list1[i].notes?.add(data)
                }
            }
        }
        mAdapterCustomers?.updateAdapterList(list1)
    }

    private fun getTagsforServer(tag: MarketTagsUtils): String {
        return when (tag) {
            MarketTagsUtils.bag -> {
                "BAGS"
            }
            MarketTagsUtils.bag_tag -> {
                "BAG_TAG"
            }
            MarketTagsUtils.cy2 -> {
                "CY2"
            }
            MarketTagsUtils.cy4 -> {
                "CY4"
            }
            MarketTagsUtils.toter -> {
                "TOTER"
            }
            MarketTagsUtils.install -> {
                "INSTALL"
            }
            MarketTagsUtils.pickup -> {
                "PICK_UP"
            }
            MarketTagsUtils.extrapump -> {
                "EXTRA_PUMP_OUT"
            }
            MarketTagsUtils.trailerpump -> {
                "TRAILER_PUMP_OUT"
            }

            else -> {
                "NO_GARBAGE"
            }
        }
    }

    fun ExitAlert(message: String, header: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(getContainerActivity())
        with(builder)
        {
            setTitle(header)
            setMessage(message)
            setCancelable(false)
            setPositiveButton(
                getString(R.string.ok),
                DialogInterface.OnClickListener { dialog, which ->
                    fragmentManager?.popBackStackImmediate()
                    dialog.dismiss()
                })
            show()
        }
    }
}