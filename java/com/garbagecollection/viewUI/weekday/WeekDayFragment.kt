package com.garbagecollection.viewUI.weekday


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garbagecollection.R
import com.garbagecollection.app.GCBaseFragment
import com.garbagecollection.base.adapter.RecyclerCallback
import com.garbagecollection.base.adapter.RecyclerViewGenricAdapter
import com.garbagecollection.common.GCCommon
import com.garbagecollection.databinding.FragmentWeekDayBinding
import com.garbagecollection.databinding.WeekDayViewBinding
import com.garbagecollection.utils.PrefUtils
import com.garbagecollection.viewUI.map.room_db.MapViewModel
import com.garbagecollection.viewUI.types.GarbageDetailType
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

class WeekDayFragment : GCBaseFragment<FragmentWeekDayBinding>() {

    private var mAdapter: RecyclerViewGenricAdapter<String, WeekDayViewBinding>? = null
    private var list: ArrayList<String> = ArrayList()
    var todayDay: String? = null
    var prevDay: String? = null
    var nextDay: String? = null
    var dayResponse: String? = null
    var modelDays: WeekDayData? = null
    private var isCheck = false


    override fun getLayoutId(): Int {
        return R.layout.fragment_week_day
    }

    private val vm by lazy {
        ViewModelProvider(this).get(MapViewModel::class.java)
    }

    override fun getCurrentFragment(): Fragment {
        return this@WeekDayFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCheck = false
        if (mAdapter == null) {
            val activeLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            viewDataBinding!!.weekDAyRecView.layoutManager = activeLayoutManager

            if (!GCCommon.isNetworkAvailable(getContainerActivity())) {
                calWeekDayFromLocal()
            } else {
                callGetWeekDay()
            }
        }
    }

    var lastClickedPos = -1
    fun callWeekdaysAdapter() {

        mAdapter = RecyclerViewGenricAdapter(
            list,
            R.layout.week_day_view,
            object : RecyclerCallback<WeekDayViewBinding, String> {
                override fun bindData(
                    binder: WeekDayViewBinding,
                    model: String,
                    position: Int,
                    itemView: View?
                ) {

                    binder.apply {
                        mondayTv.text = model

                        if (lastClickedPos == position && todayDay !=model) {
                            mondayTv.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.white
                                )
                            )
                            mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg_selected_exclude_cd)

                        }
                       else  if (todayDay==model){
                            mondayTv.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.white
                                )
                            )
                            mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg_selected)
                        }

                        else{
                            mondayTv.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.bg_color
                                )
                            )
                            mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg)
                        }
                        mondayTv.setOnClickListener {
                                lastClickedPos = position
                                mAdapter?.notifyDataSetChanged()
                                val frag = GarbageDetailType()
                                val bundle = Bundle()
                                bundle.putString("day", model.capitalize())
                                bundle.putString("dayResponse", dayResponse)
                                frag.arguments = bundle
                                displayIt(frag, frag::class.java.canonicalName, true, true)

                           /* if (!isCheck) {
                                mondayTv.setTextColor(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.white
                                    )
                                )
                                mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg_selected)
                                isCheck
                            }*/

                        }
                    }

                }
            })
        viewDataBinding!!.weekDAyRecView.adapter = mAdapter
        mAdapter?.notifyDataSetChanged()
    }

    private fun callGetWeekDay() {
        showLoading()
        list.clear()
        disposable.add(
            getApiService()!!.getWeekDay()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<WeekDayData>() {
                    override fun onSuccess(model: WeekDayData) {
                        list.addAll(model.data?.weekDays!!)
                        todayDay = model.data.today!!.lowercase(Locale.getDefault())
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }


                        dayResponse = Gson().toJson(model)
                        lastClickedPos = list.indexOf(todayDay)
                        PrefUtils.storeUserInfo(getContainerActivity(), model)

                        if (list.size > 0) {
                            callWeekdaysAdapter()
                            hideLoading()
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("logE", "" + e)
                    }
                })
        )
    }

    fun calWeekDayFromLocal() {
        list.clear()
        modelDays = PrefUtils.retrieveUserInfo(getContainerActivity())
        list.addAll(modelDays!!.data!!.weekDays!!)
        todayDay = modelDays!!.data!!.today!!.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        if (list.size > 0) {
            callWeekdaysAdapter()
        }
    }



}