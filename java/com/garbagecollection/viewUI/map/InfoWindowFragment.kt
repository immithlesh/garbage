package com.garbagecollection.viewUI.map

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devs.readmoreoption.ReadMoreOption
import com.garbagecollection.R
import com.garbagecollection.app.GCBaseFragment
import com.garbagecollection.app.GarbageColectionApplication
import com.garbagecollection.base.adapter.RecyclerCallback
import com.garbagecollection.base.adapter.RecyclerViewGenricAdapter
import com.garbagecollection.common.GCCommon
import com.garbagecollection.common.GCCommon.Companion.MY_MARKET_INFO
import com.garbagecollection.common.GCCommon.Companion.selectedGarbage
import com.garbagecollection.common.mapUtils.CustomSoredMarkers
import com.garbagecollection.common.mapUtils.InfoWindowTags
import com.garbagecollection.common.mapUtils.MarkersInfo
import com.garbagecollection.common.mapUtils.MarketTagsUtils
import com.garbagecollection.databinding.FragmentInfoWindowBinding
import com.garbagecollection.databinding.NotesViewBinding
import com.garbagecollection.utils.PrefUtils
import com.garbagecollection.viewUI.GarbageCollectionPostData
import com.garbagecollection.viewUI.map.room_db.GCDriverModel1
import com.garbagecollection.viewUI.map.room_db.Note
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.add_notes_dialog.*
import kotlinx.android.synthetic.main.add_notes_dialog.view.*
import kotlinx.android.synthetic.main.fragment_collected_garbage_dialog.*
import kotlinx.android.synthetic.main.fragment_collected_garbage_dialog.view.*
import kotlinx.android.synthetic.main.fragment_info_window.*
import kotlinx.android.synthetic.main.fragment_info_window.view.*
import kotlinx.android.synthetic.main.garbage_count_view.*
import kotlinx.android.synthetic.main.garbage_count_view_btn.view.*
import kotlinx.android.synthetic.main.view_notes_dialog.view.*
import java.util.*
import kotlin.collections.ArrayList


class InfoWindowFragment(
    val fragment: MapContainerFragment,
    val data: Any?,
    val callback: onDismissLister
) :
    GCBaseFragment<FragmentInfoWindowBinding>() {

    private var mAdapterNotes: RecyclerViewGenricAdapter<com.garbagecollection.viewUI.map.room_db.Note, NotesViewBinding>? =
        null
    private var listNotes: ArrayList<Note> = ArrayList()

    var Day: String? = null
    var addNotes: String? = null
    var selectedPos = -1
    var BottomList = ArrayList<BottomSheetModel>()
    var customer: String? = null
    var notes: String? = null
    val garbageCollectedModel = GarbageCollectionPostData()
    var markersInfoTags = ArrayList<MarkersInfo>()
    var selectedTag = MarketTagsUtils.none
    var mSelectedInfoWindowTagObj: InfoWindowTags? = null
    var model: GCDriverModel1? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_info_window
    }

    interface onDismissLister {
        fun onBottomInfoDismiss() {}
        fun onSubmitListener(id: Int) {}
    }

    override fun getCurrentFragment(): Fragment {
        return this@InfoWindowFragment
    }


    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GCCommon.closeBtnBoolean = false

        callMain()
    }


    fun callMain() {
        viewDataBinding!!.apply {
            model = Gson().fromJson(data.toString(), GCDriverModel1::class.java)
            if (model != null) {
                boisAreaTVID.text = model!!.name
                residentialIDTV.text = model!!.detailType.toString().replace("_", " ")
                addressTV.text = model!!.location
                customer = model!!.id.toString()

                listNotes.addAll(model!!.notes!!)
                val a = listNotes.size

                var type: String? = null
                val savedValue = PrefUtils.getSaveValue(
                    requireActivity(), MY_MARKET_INFO
                )
                if (savedValue != "") {
                    val obj = Gson().fromJson(savedValue, CustomSoredMarkers::class.java)
                    markersInfoTags = obj.data
                } else {
                    markersInfoTags.clear()
                }

                if (residentialIDTV.text == "RESIDENTIAL" || residentialIDTV.text == "COMMERCIAL") {
                    type = "CUSTOMER"
                    cardLinearLAyoutId.visibility = View.VISIBLE
                    cardLinearLAyoutId2.visibility = View.GONE
                    bagL.setOnClickListener {

                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }
                        GCCommon.selectedGarbage ="BAGS"
                        selectedTag = MarketTagsUtils.bag
                        GCCommon.install = "BAGS"
                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    bagTagL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }
                        GCCommon.selectedGarbage ="BAG TAG"
                        selectedTag = MarketTagsUtils.bag_tag
                        GCCommon.install = "BAG_TAG"
                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    fourCyL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }
                        selectedTag = MarketTagsUtils.cy4
                        GCCommon.install = "CY4"
                        GCCommon.selectedGarbage ="4CY"
                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    twoTYL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }
                        GCCommon.selectedGarbage ="2CY"
                        GCCommon.install = "CY2"
                        selectedTag = MarketTagsUtils.cy2
                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    totarL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }
                        GCCommon.selectedGarbage ="TOTER"
                        GCCommon.install = "TOTER"
                        garbageCollectedModel.collectionType = GCCommon.install
                        selectedTag = MarketTagsUtils.toter
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                } else if (residentialIDTV.text == "CARDBOARD") {
                    type = "CARDBOARD"
                    cardLinearLAyoutId.visibility = View.VISIBLE
                    cardLinearLAyoutId2.visibility = View.GONE
                    bagL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }
                        GCCommon.selectedGarbage ="BAGS"
                        selectedTag = MarketTagsUtils.bag
                        GCCommon.install = "BAGS"

                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    bagTagL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }

                        selectedTag = MarketTagsUtils.bag_tag
                        GCCommon.install = "BAG_TAG"
                        GCCommon.selectedGarbage ="BAG TAG"
                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    fourCyL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }

                        selectedTag = MarketTagsUtils.cy4
                        GCCommon.install = "CY4"
                        GCCommon.selectedGarbage ="4CY"
                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    twoTYL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }

                        GCCommon.install = "CY2"
                        GCCommon.selectedGarbage ="2CY"
                        selectedTag = MarketTagsUtils.cy2
                        garbageCollectedModel.collectionType = GCCommon.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    totarL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }

                        GCCommon.install = "TOTER"
                        GCCommon.selectedGarbage ="TOTER"
                        garbageCollectedModel.collectionType = GCCommon.install
                        selectedTag = MarketTagsUtils.toter
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                } else if (residentialIDTV.text == "PUBLIC TOILETS") {
                    type = "TOILET"
                    cardLinearLAyoutId.visibility = View.GONE
                    cardLinearLAyoutId2.visibility = View.VISIBLE
                    installL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }
                        GCCommon.install = "INSTALL"
                        garbageCollectedModel.collectionType = GCCommon.install
                        selectedTag = MarketTagsUtils.install
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    pickUpL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }

                        GCCommon.install = "PICK_UP"
                        garbageCollectedModel.collectionType = GCCommon.install
                        selectedTag = MarketTagsUtils.pickup
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    extraPumpL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }

                        GCCommon.install = "EXTRA_PUMP_OUT"
                        selectedTag = MarketTagsUtils.extrapump
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        garbageCollectedModel.collectionType = GCCommon.install
                        callCollectedGarbage()
                        clickableFalse()
                    }
                    trailerPumpL.setOnClickListener {
                        if (fragment.checkIsSubmitted()) {
                            return@setOnClickListener
                        }

                        GCCommon.install = "TRAILER_PUMP_OUT"
                        selectedTag = MarketTagsUtils.trailerpump
                        mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                        garbageCollectedModel.collectionType = GCCommon.install
                        callCollectedGarbage()
                        clickableFalse()
                    }
                }

                if (listNotes.size == 0) {
                    viewNotesId.visibility = View.GONE
                } else {
                    viewNotesId.visibility = View.VISIBLE
                }
                viewNotesId.setOnClickListener {
                    viewNotes()
                }
                noGarbage.setOnClickListener {
                    if (fragment.checkIsSubmitted()) {
                        return@setOnClickListener
                    }

                    selectedTag = MarketTagsUtils.nogarbage
                    mSelectedInfoWindowTagObj = getInfoTag(selectedTag, model!!.id)
                    callCollectedGarbage()


                }

                garbageCollectedModel.type = type
                garbageCollectedModel.id = model!!.id

            }
        }
    }

    private fun getInfoTag(selectedTag: MarketTagsUtils, id: Int?): InfoWindowTags? {
        if (markersInfoTags.size == 0) {
            return null
        }
        val filterObj = markersInfoTags.find {
            it.id == id!!
        } ?: return null

        val obj = filterObj.data.find {
            it.tag == selectedTag
        } ?: return null

        selectedPos = obj.selectedPos
        Log.e("getInfoTag", "${selectedPos}")
        return obj

    }

    interface MyMessageDialogListener {
        fun onClosed(
            note: String?
        )
    }

    var notString: String? = ""

    private fun callCollectedGarbage() {
        var adapter: CountAdapter? = null
        val dialog = BottomSheetDialog(getContainerActivity(), R.style.BottomSheetDialog_Rounded)
        val v = layoutInflater.inflate(R.layout.fragment_collected_garbage_dialog, null)
        val recyclerView = v.findViewById<RecyclerView>(R.id.collected_garbage_RV)
        val btnClose = v.findViewById<ImageView>(R.id.closeDialogId)
        val btnAddNotes = v.findViewById<ImageView>(R.id.addNotesId)
        val tvId = v.findViewById<MaterialTextView>(R.id.tvId)
        val mannualEditText = v.findViewById<EditText>(R.id.mannualEditText)


        val submit_btn = v.findViewById<AppCompatButton>(R.id.submit_btn)
        if (mSelectedInfoWindowTagObj == null) {
            selectedPos = -1
            notString = ""
        }
        else {

            recyclerView.visibility=if (selectedPos <=10) View.VISIBLE else View.GONE
            mannualEditText.visibility=if (selectedPos >10) View.VISIBLE else View.GONE
            if (mannualEditText.visibility == View.VISIBLE){
                mannualEditText.setText("$selectedPos")
            }

            notString = mSelectedInfoWindowTagObj?.notes ?: ""
            submit_btn.text = "Update"
        }
        btnAddNotes.setOnClickListener {
            addNotes(object : MyMessageDialogListener {
                override fun onClosed(note: String?) {
                    notString = note
                }
            }, notString)
        }

        if (selectedTag != MarketTagsUtils.nogarbage&&selectedTag != MarketTagsUtils.extrapump&&selectedTag != MarketTagsUtils.pickup&&
            selectedTag != MarketTagsUtils.install&&selectedTag != MarketTagsUtils.trailerpump) {
            tvId.visibility = View.VISIBLE
            tvId.setText("Please select the number of $selectedGarbage")

            if (selectedPos == -1) {
                for (i in 1..10) {
                    BottomList.add(BottomSheetModel(i))
                }
            } else {
                val endPoint = if (selectedPos < 10) 10 else selectedPos + 1
                for (i in 1..endPoint) {
                    BottomList.add(BottomSheetModel(i))
                }
            }
            //}
            adapter = CountAdapter(selectedPos, BottomList,object:CountAdapter.onClickAdd{
                override fun onClick() {
                    mannualEditText.visibility=View.VISIBLE
                    recyclerView.visibility=View.GONE
                }

            })
            val activeLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            recyclerView.layoutManager = activeLayoutManager
            recyclerView.adapter = adapter
            //    adapter.notifyDataSetChanged()
            Log.e("markersInfoTags", "${markersInfoTags.size}")
        } else {
            tvId.visibility = View.GONE
        }
        submit_btn.setOnClickListener {

            try {
                if (recyclerView.visibility==View.VISIBLE){
                    garbageCollectedModel.noOfBagsCollected = adapter!!.selectedPosition + 1
                }
                else{
                    garbageCollectedModel.noOfBagsCollected =mannualEditText.text.toString().toInt()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            /*  if (GCCommon.isNetworkAvailable(getContainerActivity())) {*/

            if (recyclerView.visibility==View.VISIBLE && adapter != null && adapter!!.selectedPosition + 1 == 0) {
                showToast("Please select the count")
            } else if (mannualEditText.visibility == View.VISIBLE && TextUtils.isEmpty(mannualEditText.text)){
                Toast.makeText(activity, "Please select the count", Toast.LENGTH_SHORT).show()
            }else {
                 if (submit_btn.text == "Update") {
                    if (selectedTag == MarketTagsUtils.nogarbage) {
                        showToast("Updated successfully")
                    } else {
                        showToast("Garbage Updated Successfully")
                    }
                } else {
                     if (selectedTag == MarketTagsUtils.nogarbage) {
                        showToast("Added successfully")
                    } else {
                         showToast("Garbage Added Successfully")
                     }
                }
                if (markersInfoTags.size == 0 || mSelectedInfoWindowTagObj == null) {
                    val infoMarketList = ArrayList<InfoWindowTags>()
                    val mMarkersInfo = ArrayList<MarkersInfo>()
                    val obj =
                        InfoWindowTags(if (recyclerView.visibility == View.VISIBLE) {adapter?.selectedPosition ?: -1}else{mannualEditText.text.toString().toInt()}, notString!!, selectedTag)
                    infoMarketList.add(obj)
                    val data = MarkersInfo(model!!.id!!, infoMarketList)
                    if (markersInfoTags.size == 0) {
                        markersInfoTags.add(data)
                    } else {
                        val pos = getFilterPos(markersInfoTags)
                        if (pos != -1) {
                            val pos1 = getSubFilterPos(markersInfoTags[pos])
                            if (pos1 != -1) {
                                markersInfoTags[pos] = data
                            } else {
                                val objNew = markersInfoTags[pos].data
                                objNew.add(obj)
                                val newD = MarkersInfo(model!!.id!!, objNew)
                                markersInfoTags[pos] = newD
                            }

                        } else {
                            markersInfoTags.add(data)
                        }

                    }
                    val mData = CustomSoredMarkers(markersInfoTags)
                    val json = Gson().toJson(mData)
                    PrefUtils.setSaveValue(requireActivity(), MY_MARKET_INFO, json
                    )
                } else {
                    val obj =
                        InfoWindowTags(if (recyclerView.visibility == View.VISIBLE) {adapter?.selectedPosition ?: -1}else{mannualEditText.text.toString().toInt()}, notString!!, selectedTag)
                    val pos = getFilterPos(markersInfoTags)
                    val mList = markersInfoTags[pos].data
                    var listpos = -1
                    mList.withIndex().find {
                        listpos = it.index
                        it.value.tag == selectedTag
                    }
                    mList[listpos] = obj
                    markersInfoTags[pos] = MarkersInfo(model!!.id!!, mList)
                    val mData = CustomSoredMarkers(markersInfoTags)
                    val json = Gson().toJson(mData)
                    PrefUtils.setSaveValue(
                        requireActivity(),
                        MY_MARKET_INFO,
                        json
                    )

                }
                callback.onBottomInfoDismiss()
                callback.onSubmitListener(model?.id ?: -1)
                dialog.dismiss()
            }
            fragment.checkAllCustomerServed()
        }

        btnClose.setOnClickListener {
            BottomList.clear()
            adapter?.setCount(-1)
            adapter?.notifyDataSetChanged()
            dialog.dismiss()
            clickableTrue()
        }
        dialog.setCancelable(false)
        dialog.setContentView(v)
        dialog.show()
    }

    private fun getSubFilterPos(markersInfo: MarkersInfo): Int {
        var pos = -1
        for (i in markersInfo.data.indices) {
            val data = markersInfo.data[i]
            if (data.tag == selectedTag) {
                pos = i
                break
            }
        }
        return pos
    }

    private fun getFilterPos(markersInfoTags: java.util.ArrayList<MarkersInfo>): Int {
        var pos = -1
        for (i in markersInfoTags.indices) {
            val data = markersInfoTags[i]
            if (data.id == model!!.id) {
                pos = i
                break
            }
        }
        return pos
    }


    private fun addNotes(listner: MyMessageDialogListener, notString: String?) {

        val dialog2 = Dialog(getContainerActivity())
        val v1 = layoutInflater.inflate(R.layout.add_notes_dialog, null)
        val window = dialog2.window
        val edit = v1.findViewById<EditText>(R.id.addNotesEditText)
        if (this.notString != null) {
            edit.setText(this.notString)
        }
        window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        dialog2.setCancelable(false)
        dialog2.setContentView(v1)
        dialog2.show()
        v1.closeAddNotes.setOnClickListener {
            dialog2.dismiss()
        }
        if (notString != null) {
            edit.setText(notString)
        }
        v1.tooHeavy.setOnClickListener {
            v1.addNotesEditText.setText(v1.tooHeavy.text)
        }
        v1.noBagTag.setOnClickListener {
            v1.addNotesEditText.setText(v1.noBagTag.text)
        }
        v1.movedTv.setOnClickListener {
            v1.addNotesEditText.setText(v1.movedTv.text)
        }
        v1.holidayTv.setOnClickListener {
            v1.addNotesEditText.setText(v1.holidayTv.text)
        }
        v1.okBtnId.setOnClickListener {

            if (edit.text.toString()
                    .startsWith(" ") || edit.text.toString()
                    .equals(null)
            ) {
                showToast("Please add some notes")
            } else {
                addNotes = edit.text.trim().toString()
                garbageCollectedModel.notes = addNotes
                listner.onClosed(addNotes)
                showToast("notes added")
                dialog2.dismiss()
            }
        }
    }

    private fun viewNotes() {
        val dialog = Dialog(getContainerActivity())
        val v = layoutInflater.inflate(R.layout.view_notes_dialog, null)

        val activeLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        v.notesId.layoutManager = activeLayoutManager


        mAdapterNotes = RecyclerViewGenricAdapter(
            listNotes,
            R.layout.notes_view,
            object : RecyclerCallback<NotesViewBinding, Note> {
                override fun bindData(
                    binder: NotesViewBinding,
                    model: Note,
                    position: Int,
                    itemView: View?
                ) {
                    binder.apply {

                        try {
                            val readMoreOption = ReadMoreOption.Builder(getContainerActivity())
                                .textLength(4)
                                .textLengthType(ReadMoreOption.TYPE_LINE)
                                .moreLabel("Read More")
                                .lessLabel("..Read Less")
                                .moreLabelColor(Color.parseColor("#002C5B"))
                                .lessLabelColor(Color.parseColor("#002C5B"))
                                .labelUnderLine(false)
                                .expandAnimation(false)
                                .build()

                            readMoreOption.addReadMoreTo(note, model.notes ?: "")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            note.text = model.notes
                        }

                    }
                }

            })
        v.notesId.adapter = mAdapterNotes
        mAdapterNotes?.notifyDataSetChanged()

        v.closeDialogId2.setOnClickListener {
            dialog.dismiss()
        }
        val window = dialog.window
        window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        dialog.setCancelable(false)
        dialog.setContentView(v)
        dialog.show()

    }

    private fun clickableFalse() {
        viewDataBinding!!.apply {
            bagL.isClickable = false
            bagTagL.isClickable = false
            totarL.isClickable = false
            fourCyL.isClickable = false
            twoTYL.isClickable = false
        }
    }

    private fun clickableTrue() {
        viewDataBinding!!.apply {
            bagL.isClickable = true
            bagTagL.isClickable = true
            totarL.isClickable = true
            fourCyL.isClickable = true
            twoTYL.isClickable = true
        }
    }

}

class CountAdapter(
    var selectedPosition: Int,
    private val CountArray: ArrayList<BottomSheetModel>,
    private val listener:onClickAdd
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TITLE = 0
    private val ADD_MORE = 1
    override fun getItemViewType(position: Int): Int {
        return if (position < CountArray.size) {
            TITLE
        } else {
            ADD_MORE
        }
    }

    fun setCount(count: Int) {
        selectedPosition = count
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: AppCompatTextView = view.findViewById(R.id.garbage_count)
        val layout: RelativeLayout = view.findViewById(R.id.gc_back)
    }

    class ViewHolder2(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == TITLE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.garbage_count_view, parent, false)
            return ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.garbage_count_view_btn, parent, false)
            return ViewHolder2(view)
        }
    }

    override fun getItemCount(): Int {
        return CountArray.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ViewHolder) {
            holder.textView.text = CountArray[position].bagCount.toString()

            holder.layout.setOnClickListener {
                selectedPosition = position+1
                notifyDataSetChanged()
            }

            if (position < selectedPosition) {
                holder.textView.setTextColor(
                    ContextCompat.getColor(
                        GarbageColectionApplication.appContext!!,
                        R.color.white
                    )
                )
                holder.layout.setBackgroundResource(R.drawable.circular_btn_selected)

            } else {
                holder.textView.setTextColor(
                    ContextCompat.getColor(
                        GarbageColectionApplication.appContext!!,
                        R.color.bg_color
                    )
                )
                holder.layout.setBackgroundResource(R.drawable.circular_button)
            }

        } else if (holder is ViewHolder2) {
            (holder).itemView.add_btn.setOnClickListener {
                listener.onClick()
            /*var bottomSheetModel: Int = CountArray[CountArray.size - 1].bagCount
                bottomSheetModel++
                CountArray.add(BottomSheetModel(bottomSheetModel))
                notifyDataSetChanged()*/

            }
        }
    }

    interface onClickAdd{
        fun onClick()
    }

}
