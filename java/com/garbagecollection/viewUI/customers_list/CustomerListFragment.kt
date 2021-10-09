package com.garbagecollection.viewUI.customers_list

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garbagecollection.MyFirebaseMessagingService
import com.garbagecollection.R
import com.garbagecollection.app.GCBaseFragment
import com.garbagecollection.base.adapter.RecyclerCallback
import com.garbagecollection.base.adapter.RecyclerViewGenricAdapter
import com.garbagecollection.common.GCCommon
import com.garbagecollection.common.NotificationX
import com.garbagecollection.common.mapUtils.CustomSoredMarkers
import com.garbagecollection.common.mapUtils.InfoWindowTags
import com.garbagecollection.common.mapUtils.MarketTagsUtils
import com.garbagecollection.databinding.CustomersViewItemBinding
import com.garbagecollection.databinding.FragmentCustomerListBinding
import com.garbagecollection.utils.PrefUtils
import com.garbagecollection.viewUI.GarbageCollectionPostDataNew
import com.garbagecollection.viewUI.map.*
import com.garbagecollection.viewUI.map.room_db.MapViewModel
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.customers_view_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList


class CustomerListFragment : GCBaseFragment<FragmentCustomerListBinding>() {
    private var mAdapterCustomers: RecyclerViewGenricAdapter<Result, CustomersViewItemBinding>? =
        null
    var list: ArrayList<Result> = ArrayList()
    private lateinit var model: FetchMarkerModel

    var Day: String? = null
    var DetailType: String? = null
    var DayResponse: String? = null
    var directionResponse: String? = null
    var direction: String? = null
    var Route: String? = null


    override fun getLayoutId(): Int {
        return R.layout.fragment_customer_list
    }

    override fun getCurrentFragment(): Fragment {
        return this@CustomerListFragment
    }

    override fun onPause() {
        super.onPause()
        MyFirebaseMessagingService.mNotifyCurrentFrag = null
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MyFirebaseMessagingService.mNotifyCurrentFrag = this@CustomerListFragment
        val gpsTracker = GPSTracker(getContainerActivity())
        gpsTracker.startTracking()
        callCustomersListAdapter()
        val bundle = this.arguments
        if (bundle != null) {
            Day = bundle.getString("day")
            DetailType = bundle.getString("detailType")
            DayResponse = bundle.getString("dayResponse")
            Route = bundle.getString("route")
            viewDataBinding!!.selectedDayTv.text = Day
            viewDataBinding!!.SelectedRouteTVId.text = Route

        }

        if (GCCommon.isNetworkAvailable(getContainerActivity())) {
            callGetCustomers(
                DetailType!!,
                viewDataBinding!!.SelectedRouteTVId.text.toString(),
                viewDataBinding!!.selectedDayTv.text.toString(),
                gpsTracker.getLatitude(), gpsTracker.getLongitude()
            )
        } else {
            fromLocalDB()
        }

        viewDataBinding?.apply {
            viewMapId.setOnClickListener {
                val frag = MapContainerFragment()
                val bundle = Bundle()
                bundle.putString("day", Day)
                bundle.putString("route", Route)
                bundle.putString("detailType", DetailType)
                bundle.putString("directionResponse", directionResponse)
                bundle.putString("DayResponse", DayResponse)
                frag.arguments = bundle
                displayIt(frag, frag::class.java.canonicalName, true, true)
            }
            backIcon.setOnClickListener {
                val builder = androidx.appcompat.app.AlertDialog.Builder(getContainerActivity())
                with(builder)
                {
                    setTitle("Garbage Collection")
                    setMessage("Do you want to move out ?")
                    setCancelable(false)
                    setPositiveButton(
                        getString(R.string.yes),
                        DialogInterface.OnClickListener { dialog, which ->
                            fragmentManager?.popBackStack()
                        })
                    setNegativeButton(
                        getString(R.string.canel),
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                    show()

                }

            }
        }


    }


    private fun callGetCustomers(
        type: String,
        route: String,
        selectedDay: String,
        latitude: Double,
        longitude: Double
    ) {
        showLoading()
        list.clear()
        disposable.add(
            getApiService()!!.getFilterMap(type, route, selectedDay, latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<FetchMarkerModel>() {
                    override fun onSuccess(model: FetchMarkerModel) {
                        this@CustomerListFragment.model = model
                        list.addAll(model.data?.results!!)
                        checkAllCustomerServed()

                        if (list.size > 0) {

                            val filter = list.filter { it.isGarbageSubmit }
                            setSubmittedValue(!filter.isNullOrEmpty())
                            mAdapterCustomers?.updateAdapterList(list as java.util.ArrayList<Result>)
                            hideLoading()
                        } else {
                            hideLoading()
                            ExitAlert("No any customers in this route", "Garbage Collection")
                        }

                    }

                    override fun onError(e: Throwable) {
                        Log.d("logE", "" + e)
                    }
                })
        )
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

    private fun setSubmittedValue(isSubmit: Boolean) {
        isSubmitted = flow {
            emit(isSubmit)
        }.flowOn(Dispatchers.Default)
    }


    private val vm by lazy {
        ViewModelProvider(this).get(MapViewModel::class.java)
    }

    fun callCustomersListAdapter() {
        val savedValue = PrefUtils.getSaveValue(
            requireActivity(), GCCommon.MY_MARKET_INFO
        )
        val obj = Gson().fromJson(
            savedValue,
            CustomSoredMarkers::class.java
        )

        mAdapterCustomers = RecyclerViewGenricAdapter(
            list,
            R.layout.customers_view_item,
            object : RecyclerCallback<CustomersViewItemBinding, Result> {
                override fun bindData(
                    binder: CustomersViewItemBinding,
                    model: Result,
                    position: Int,
                    itemView: View?
                ) {

                    binder.apply {
                        customerNameId.text = model.name
                        customerAddressId.text = model.location

                        if (!model.isNew){
                            customerNameId.setTextColor(context!!.getColor(R.color.green2))
                        }
                        else{
                            customerNameId.setTextColor(context!!.getColor(R.color.bg_color))
                        }
                        if (!model.isGarbageSubmit) {
                            if (obj != null && !obj.data.isNullOrEmpty()) {
                                val isKeyExist = obj.data.find {
                                    it.id == model.id
                                }
                                if (isKeyExist != null) {
                                    checkedId.visibility = View.VISIBLE
                                } else {
                                    checkedId.visibility = View.GONE
                                }
                            }
                        } else {
                            checkedId.visibility = View.VISIBLE
                        }

                        itemView!!.setOnClickListener {
                            val gson = Gson()
                            val json = gson.toJson(model)
                            val savedValue = PrefUtils.getSaveValue(
                                requireActivity(),
                                GCCommon.MY_MARKET_INFO
                            )
                            val data = Gson().fromJson(savedValue, InfoWindowTags::class.java)
                            val mainInfoDialog =
                                CustomerDialogMain(this@CustomerListFragment, json, object :
                                    CustomerDialogMain.onDismissLister {

                                    override fun onSubmitListener(id: Int) {
                                        if (id != -1) {
                                            checkedId.visibility = View.VISIBLE
                                        }
                                    }
                                })
                            mainInfoDialog.isCancelable = false
                            mainInfoDialog.show(fragmentManager!!.beginTransaction(), "")

                        }
                        if (model.isNewKey) {
                            notesAddedIconId.visibility = View.VISIBLE
                            if (model.isNew){
                                notesAddedIconId.setColorFilter(ContextCompat.getColor(getContainerActivity(), R.color.bg_color), android.graphics.PorterDuff.Mode.MULTIPLY)
                            }
                            else{
                                notesAddedIconId.setColorFilter(ContextCompat.getColor(getContainerActivity(), R.color.green2), android.graphics.PorterDuff.Mode.MULTIPLY)
                            }
                        } else {
                            notesAddedIconId.visibility = View.GONE
                        }
                    }

                }
            })
        val activeLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        viewDataBinding!!.customersRecyclerViewId.layoutManager = activeLayoutManager
        viewDataBinding!!.customersRecyclerViewId.adapter = mAdapterCustomers
    }

    fun fromLocalDB() {
        vm.getFinalMapData(1, Day!!, Route!!, DetailType!!)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                if (it == null) {
                    return@Observer
                }

                model = Gson().fromJson<FetchMarkerModel>(
                    it.backendResponse,
                    FetchMarkerModel::class.java
                )

                if (model != null) {
                    list.clear()
                    list.addAll(model.data!!.results!!)
                    val filter = list.filter { it.isGarbageSubmit }
                    setSubmittedValue(!filter.isNullOrEmpty())
                    mAdapterCustomers?.updateAdapterList(list)
                    checkAllCustomerServed()
                }
            })
    }

    fun checkAllCustomerServed() {
        viewDataBinding?.finalSubmitFromList?.visibility = View.GONE

        val savedValue = PrefUtils.getSaveValue(
            requireActivity(), GCCommon.MY_MARKET_INFO
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

        viewDataBinding?.finalSubmitFromList?.visibility = View.VISIBLE
        finalSubmitToServer()
    }

    val garbageCollectedModel = ArrayList<GarbageCollectionPostDataNew>()

    fun finalSubmitToServer() {
        viewDataBinding?.finalSubmitFromList?.setOnClickListener {
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

            if (GCCommon.isNetworkAvailable(getContainerActivity())) {
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

    fun updateFrag(payload: NotificationX?) {
        getContainerActivity().runOnUiThread {
            val id = payload?.tag?.toInt()
            for (i in list.indices) {
                val listId = list[i].id
                if (listId == id) {
                    list[i].isNewKey = true
                    val data = Note("", listId, payload!!.notes)
                    list[i].notes?.add(data)
                }
            }
            mAdapterCustomers?.updateAdapterList(list)
        }

    }

    fun ExitAlert(message: String, header: String) {
        val builder = AlertDialog.Builder(getContainerActivity())
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