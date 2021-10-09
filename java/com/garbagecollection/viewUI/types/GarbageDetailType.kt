package com.garbagecollection.viewUI.types

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garbagecollection.R
import com.garbagecollection.app.GCBaseFragment
import com.garbagecollection.base.adapter.RecyclerCallback
import com.garbagecollection.base.adapter.RecyclerViewGenricAdapter
import com.garbagecollection.common.GCCommon
import com.garbagecollection.databinding.GarbageDetailTypeBinding
import com.garbagecollection.databinding.WeekDayViewBinding
import com.garbagecollection.utils.PrefUtils
import com.garbagecollection.viewUI.route.RouteFragment
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.fragment_map_container.*
import kotlinx.android.synthetic.main.fragment_route.*
import kotlinx.android.synthetic.main.fragment_route.backIcon
import kotlinx.android.synthetic.main.week_day_view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class GarbageDetailType : GCBaseFragment<GarbageDetailTypeBinding>() {
    private var mAdapter: RecyclerViewGenricAdapter<GarbageTypeModel, WeekDayViewBinding>? = null
    private var list: ArrayList<GarbageTypeModel> = ArrayList()
    override fun getLayoutId(): Int {
        return R.layout.garbage_detail_type
    }
    var Day: String? = null
    var DayResponse: String? = null
    var detailTypeResponse: String? = null
    var detailType: String? = null
    var type: String? = null
    override fun getCurrentFragment(): Fragment {
        return this@GarbageDetailType
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            val bundle = this.arguments
            if (bundle != null) {
                Day = bundle.getString("day")
                DayResponse = bundle.getString("dayResponse")
                viewDataBinding!!.selectedDayTv.text = Day
            }
        list.clear()
        list.add(GarbageTypeModel("Residential + Commercial"))
        list.add(GarbageTypeModel("Public Toilet"))
        list.add(GarbageTypeModel("Cardboard"))
        callAdapter()

            val activeLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            viewDataBinding!!.RouteRecyclerView.layoutManager = activeLayoutManager

        viewDataBinding!!.backIcon.setOnClickListener {
            fragmentManager?.popBackStack()
        }

    }

    private lateinit var textViewT:MaterialTextView


    fun callAdapter() {
        mAdapter = RecyclerViewGenricAdapter(
            list,
            R.layout.week_day_view,
            object : RecyclerCallback<WeekDayViewBinding, GarbageTypeModel> {
                override fun bindData(
                    binder: WeekDayViewBinding,
                    model: GarbageTypeModel,
                    position: Int,
                    itemView: View?
                ) {


                    binder.apply {
                        mondayTv.text = model.garbageType
                        mondayTv.setOnClickListener {
                            textViewT=mondayTv
                            type=mondayTv.text.toString()
                            if (type=="Residential + Commercial"){
                                detailType="CUSTOMER"
                            }
                            else if (type=="Public Toilet"){
                                detailType="TOILET"
                            }
                            else if (type=="Cardboard"){
                                detailType="CARDBOARD"
                            }
                            mondayTv.setTextColor(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.white
                                )
                            )
                            mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg_selected)
                            val frag = RouteFragment()
                            val bundle = Bundle()
                            bundle.putString("day", Day)
                            bundle.putString("detailType", detailType)
                            bundle.putString("detailTypeResponse", detailTypeResponse)
                            bundle.putString("DayResponse", DayResponse)
                            frag.arguments = bundle
                            PrefUtils.clear(
                                requireActivity(), GCCommon.MY_MARKET_INFO
                            )
                            displayIt(frag, frag::class.java.canonicalName, true, true)
                        }
                    }

                }
            })
        viewDataBinding!!.RouteRecyclerView.adapter = mAdapter
        mAdapter?.notifyDataSetChanged()
    }

}
