/*
package com.garbagecollection.viewUI.weekday

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.garbagecollection.R
import kotlinx.android.synthetic.main.week_day_view.view.*

class WeekDaysAdapter(
    val list: ArrayList<Data.Days>,
    val weekDayFragment: WeekDayFragment,
    val todayDay: String?,
    val requireActivity: FragmentActivity
) : RecyclerView.Adapter<WeekDaysAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.week_day_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.mondayTv.setText(list[position].name)
        if (list.get(position).name == todayDay) {
            holder.mondayTv.setTextColor(ContextCompat.getColor(requireActivity, R.color.white))
            holder.mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg_selected)
        }

        if (list[position].isSelected) {
            holder.mondayTv.setTextColor(ContextCompat.getColor(requireActivity, R.color.white))
            holder.mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg_selected)
        } else {
            holder.mondayTv.setTextColor(ContextCompat.getColor(requireActivity, R.color.bg_color))
            holder.mondayTv.setBackgroundResource(R.drawable.day_week_btn_bg)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mondayTv = view.mondayTv

        init {
          itemView.setOnClickListener {
              //weekDayFragment.itemClick(adapterPosition)
          }
        }
    }
}*/
