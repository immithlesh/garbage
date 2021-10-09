package com.garbagecollection.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

import java.util.*

class RecyclerViewGenricAdapter<T, VM : ViewDataBinding?>(
    private var items: ArrayList<T>,
    private val layoutId: Int,
    private var bindingInterface: RecyclerCallback<VM, T>
) :
    RecyclerView.Adapter<RecyclerViewGenricAdapter<T, VM >.RecyclerViewHolder>() {

    inner class RecyclerViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        var binding: VM? = DataBindingUtil.bind(view!!)
        fun bindData(model: T, pos: Int) {
            bindingInterface.bindData(binding!!, model, pos, itemView)
        }

        init {
            binding = DataBindingUtil.bind(view!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return RecyclerViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.setTag(position)
        holder.bindData(item, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateAdapterList(items: ArrayList<T>) {
        this.items = items
        notifyDataSetChanged()
    }

    init {
        this.bindingInterface = bindingInterface
    }

}
