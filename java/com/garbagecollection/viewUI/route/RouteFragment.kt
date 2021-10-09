package com.garbagecollection.viewUI.route

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garbagecollection.R
import com.garbagecollection.app.GCBaseFragment
import com.garbagecollection.base.adapter.RecyclerCallback
import com.garbagecollection.base.adapter.RecyclerViewGenricAdapter
import com.garbagecollection.common.GCCommon
import com.garbagecollection.databinding.FragmentRouteBinding
import com.garbagecollection.databinding.WeekDayViewBinding
import com.garbagecollection.utils.PrefUtils
import com.garbagecollection.viewUI.customers_list.CustomerListFragment
import com.garbagecollection.viewUI.map.room_db.GCDriverModel
import com.garbagecollection.viewUI.map.room_db.MapViewModel
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_map_container.*
import kotlinx.android.synthetic.main.fragment_route.*
import kotlinx.android.synthetic.main.week_day_view.*
import kotlinx.coroutines.*
import java.lang.Exception

class RouteFragment : GCBaseFragment<FragmentRouteBinding>() {
    private var mAdapter: RecyclerViewGenricAdapter<Route, WeekDayViewBinding>? = null
    private var list: ArrayList<Route> = ArrayList()
    override fun getLayoutId(): Int {
        return R.layout.fragment_route
    }

    private val vm by lazy {
        ViewModelProvider(this).get(MapViewModel::class.java)
    }

    var Day: String? = null
    var DetailTYpe: String? = null
    var DayResponse: String? = null
    var directionResponse: String? = null
    var direction: String? = null
    var savedValue:String?=null
    override fun getCurrentFragment(): Fragment {
        return this@RouteFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleObserver()
        val bundle = this.arguments
        if (bundle != null) {
            Day = bundle.getString("day")
            DetailTYpe = bundle.getString("detailType")
            DayResponse = bundle.getString("dayResponse")
            viewDataBinding!!.selectedDayTv.text = Day
        }


         savedValue = PrefUtils.getSaveValue(
            requireActivity(), "route")


        if (!GCCommon.isNetworkAvailable(getContainerActivity())) {
            callGetRouteLocal()
        } else {
            callgetRoute()

        }

        val activeLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        viewDataBinding!!.RouteRecyclerView.layoutManager = activeLayoutManager

        viewDataBinding!!.backIcon.setOnClickListener {
            fragmentManager?.popBackStack()
        }

    }

    private lateinit var textViewT: MaterialTextView

    private fun handleObserver() {
        vm.getupdateListener.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (vm.gcwebDriverData == null) {

                    AlertDialog.Builder(requireActivity()).setTitle("Alert")
                        .setMessage("Don't have data. Please turn on your internet connection")
                        .setPositiveButton(
                            "Ok"
                        ) { dialog, which ->
                            textViewT.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.bg_color
                                )
                            )
                            textViewT.setBackgroundResource(R.drawable.day_week_btn_bg)

                            dialog.dismiss()
                        }.create().show()
                } else {
                    Log.e("IRan","Observer")
                    val frag = CustomerListFragment()
                    val bundle = Bundle()
                    bundle.putString("day", Day)
                    bundle.putString("route", direction)
                    bundle.putString("detailType", DetailTYpe)
                    bundle.putString("directionResponse", directionResponse)
                    bundle.putString("DayResponse", DayResponse)
                    frag.arguments = bundle
                    displayIt(frag, frag::class.java.canonicalName, true, true)
                }
                vm.getupdateListener.value = false
            }
        })
    }

    fun callRouteAdapter() {
        mAdapter = RecyclerViewGenricAdapter(
            list,
            R.layout.week_day_view,
            object : RecyclerCallback<WeekDayViewBinding, Route> {
                override fun bindData(
                    binder: WeekDayViewBinding,
                    model: Route,
                    position: Int,
                    itemView: View?
                ) {

                    binder.apply {
                        mondayTv.text = model.routeName
                        mondayTv.setOnClickListener {
                            textViewT = mondayTv
                            direction = mondayTv.text.toString()
                            mondayTv.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.white
                                )
                            )
                            mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg_selected)
                            if (!GCCommon.isNetworkAvailable(getContainerActivity())) {

                                /*GlobalScope.launch(Dispatchers.IO) {
                                    vm.getExistingDriver(
                                        1,
                                        mondayTv.text.toString(),
                                        Day!!,
                                        DetailTYpe!!,
                                        getContainerActivity()
                                    )
                                    this.cancel()
                                }*/
                                val frag = CustomerListFragment()
                                val bundle = Bundle()
                                bundle.putString("day", Day)
                                bundle.putString("route", direction)
                                bundle.putString("detailType", DetailTYpe)
                                bundle.putString("directionResponse", directionResponse)
                                bundle.putString("DayResponse", DayResponse)
                                frag.arguments = bundle

                                if (TextUtils.isEmpty(savedValue)){
                                    displayIt(frag, frag::class.java.canonicalName, true, true)
                                    PrefUtils.setSaveValue(getContainerActivity(),"route",direction.toString())
                                }

                                if (!TextUtils.isEmpty(savedValue)&&savedValue!=direction){
                                    val builder = androidx.appcompat.app.AlertDialog.Builder(getContainerActivity())
                                    with(builder)
                                    {
                                        setTitle("Garbage Collection")
                                        setMessage("Previously filled data will be remove if you will change your route.")
                                        setCancelable(false)
                                        setPositiveButton(
                                            getString(R.string.ok),
                                            DialogInterface.OnClickListener { dialog, which ->
                                                displayIt(frag, frag::class.java.canonicalName, true, true)
                                                PrefUtils.clear(getContainerActivity(),
                                                    GCCommon.MY_MARKET_INFO
                                                )
                                                PrefUtils.setSaveValue(getContainerActivity(),"route",direction.toString())
                                            })
                                        setNegativeButton(
                                            getString(R.string.canel),
                                            DialogInterface.OnClickListener { dialog, which ->
                                                mondayTv.setTextColor(
                                                    ContextCompat.getColor(
                                                        requireActivity(),
                                                        R.color.bg_color
                                                    )
                                                )
                                                mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg)
                                                dialog.dismiss()
                                            })
                                        show()

                                    }

                                }
                                else if (savedValue==direction){
                                    displayIt(frag, frag::class.java.canonicalName, true, true)
                                    PrefUtils.setSaveValue(getContainerActivity(),"route",direction.toString())
                                }

                            } else {
                                Log.e("IRan","Adapter")
                                val frag = CustomerListFragment()
                                val bundle = Bundle()
                                bundle.putString("day", Day)
                                bundle.putString("route", direction)
                                bundle.putString("detailType", DetailTYpe)
                                bundle.putString("directionResponse", directionResponse)
                                bundle.putString("DayResponse", DayResponse)
                                frag.arguments = bundle
                                if (TextUtils.isEmpty(savedValue)){
                                    displayIt(frag, frag::class.java.canonicalName, true, true)
                                    PrefUtils.setSaveValue(getContainerActivity(),"route",direction.toString())
                                }

                                if (!TextUtils.isEmpty(savedValue)&&savedValue!=direction){
                                    val builder = androidx.appcompat.app.AlertDialog.Builder(getContainerActivity())
                                    with(builder)
                                    {
                                        setTitle("Garbage Collection")
                                        setMessage("Previously filled data will be remove if you will change your route.")
                                        setCancelable(false)
                                        setPositiveButton(
                                            getString(R.string.ok),
                                            DialogInterface.OnClickListener { dialog, which ->
                                                displayIt(frag, frag::class.java.canonicalName, true, true)
                                                PrefUtils.clear(getContainerActivity(),
                                                    GCCommon.MY_MARKET_INFO
                                                )
                                                PrefUtils.setSaveValue(getContainerActivity(),"route",direction.toString())
                                            })
                                        setNegativeButton(
                                            getString(R.string.canel),
                                            DialogInterface.OnClickListener { dialog, which ->
                                                mondayTv.setTextColor(
                                                    ContextCompat.getColor(
                                                        requireActivity(),
                                                        R.color.bg_color
                                                    )
                                                )
                                                mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg)
                                                dialog.dismiss()
                                            })
                                        show()

                                    }

                                }
                                else if (savedValue==direction){
                                    displayIt(frag, frag::class.java.canonicalName, true, true)
                                    PrefUtils.setSaveValue(getContainerActivity(),"route",direction.toString())
                                }

                            }
                        }
                    }

                }
            })
        viewDataBinding!!.RouteRecyclerView.adapter = mAdapter
        mAdapter?.notifyDataSetChanged()
    }

    private fun callgetRoute() {
        showLoading()
        list.clear()
        disposable.add(
            getApiService()!!.getRoute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<RouteModel>() {
                    override fun onSuccess(model: RouteModel) {
                        list.addAll(model.data!!.routes!!)

                        directionResponse = Gson().toJson(model)
                        val obj = vm.existingMapDriver(1, Day!!, getContainerActivity())

                        obj.observe(viewLifecycleOwner, Observer {
                            if (it != null) {
                                //vm.updateMapDriver(GCDriverModel(it.id!!, null, directionResponse, Day!!, DayResponse, null, null))
                                GlobalScope.launch(Dispatchers.IO) {
                                    try {
                                        if (TextUtils.isEmpty(directionResponse!!) && TextUtils.isEmpty(
                                                DayResponse!!
                                            ) && TextUtils.isEmpty(Day!!) && TextUtils.isEmpty(
                                                DetailTYpe!!
                                            )
                                        )
                                            vm.updateRoute(
                                                directionResponse!!,
                                                DayResponse!!,
                                                Day!!,
                                                DetailTYpe!!,
                                                it.id!!
                                            )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                    cancel()
                                }

                            } else {
//                                    if Driver not exits
                                vm.insertMapDriver(
                                    GCDriverModel(
                                        1,
                                        null,
                                        directionResponse,
                                        Day!!,
                                        DayResponse,
                                        null,
                                        null,
                                        DetailTYpe
                                    )
                                )
                            }
                            obj.removeObservers(viewLifecycleOwner)
                        })
                        if (list.size > 0) {
                            callRouteAdapter()
                            hideLoading()
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("logE", "" + e)
                    }
                })
        )
    }

    fun  ExitAlert(){
        val builder = androidx.appcompat.app.AlertDialog.Builder(getContainerActivity())
        with(builder)
        {
            setTitle("Garbage Collection")
            setMessage("No route found for the selected day,Please turn on internet.")
            setCancelable(false)
            setPositiveButton(
                getString(R.string.ok),
                DialogInterface.OnClickListener { dialog, which ->
                    requireFragmentManager().popBackStackImmediate()
                })
            show()
        }
    }
    fun callGetRouteLocal() {
        list.clear()
        vm.getAllMapDriver(1, Day!!).observe(viewLifecycleOwner, Observer {
            try {
                val directionResponse = Gson().fromJson(it.directionResponse, RouteModel::class.java)
                if (directionResponse!=null){
                    list.addAll(directionResponse.data!!.routes!!)
                    callRouteAdapter()
                }
                else{

                }



            }
          catch (e:Exception){
              print(e)
          }
        })
    }

}

