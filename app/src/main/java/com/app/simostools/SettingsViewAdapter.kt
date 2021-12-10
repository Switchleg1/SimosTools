package com.app.simostools

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import java.lang.Exception
import androidx.recyclerview.widget.ItemTouchHelper
import java.lang.Long.parseLong

class SettingsViewAdapter internal constructor(context: Context?, data: Array<PIDStruct?>?) :
    RecyclerView.Adapter<SettingsViewAdapter.ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {
    private val TAG                                 = "SettingsViewAdapter"
    private val mData: Array<PIDStruct?>            = data?: arrayOfNulls(0)
    private val mViews: Array<ViewHolder?>          = arrayOfNulls(data?.size?:0)
    private val mInflater: LayoutInflater           = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener?  = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.fragment_settings_pid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pid = mData[position]
        holder.loadData(position, pid)
        mViews[position] = holder
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var mPID:PIDStruct?                 = null
        var mIndex:Int                      = 0
        var mNameEdit: EditText?            = null
        var mNameText: TextView?            = null
        var mUnitEdit: EditText?            = null
        var mUnitText: TextView?            = null
        var mAddressEdit: EditText?         = null
        var mAddressText: TextView?         = null
        var mLengthEdit: EditText?          = null
        var mLengthText: TextView?          = null
        var mGaugeMinEdit: EditText?        = null
        var mGaugeMinText: TextView?        = null
        var mGaugeMaxEdit: EditText?        = null
        var mGaugeMaxText: TextView?        = null
        var mWarnMinEdit: EditText?         = null
        var mWarnMinText: TextView?         = null
        var mWarnMaxEdit: EditText?         = null
        var mWarnMaxText: TextView?         = null
        var mEquationEdit: EditText?        = null
        var mEquationText: TextView?        = null
        var mFormatEdit: EditText?          = null
        var mFormatText: TextView?          = null
        var mSmoothingEdit: EditText?       = null
        var mSmoothingText: TextView?       = null
        var mAssignToEdit: EditText?        = null
        var mAssignToText: TextView?        = null
        var mEnableSwitch: SwitchCompat?    = null
        var mTabsText: TextView?            = null
        var mTabsEdit: EditText?            = null
        var mUpArrowButton: SwitchButton?   = null
        var mDownArrowButton: SwitchButton? = null
        var mDivider1: View?                = null

        fun loadData() {
            mData.forEachIndexed() { i, it ->
                if(mPID == it) {
                    loadData(i, mPID)
                    return
                }
            }
        }
        fun loadData(index: Int, pid: PIDStruct?) {
            mIndex = index
            mPID = pid
            mNameEdit?.apply {
                setText(pid?.name)
                setTextColor(ColorList.TEXT.value)
            }
            mUnitEdit?.apply {
                setText(pid?.unit)
                setTextColor(ColorList.TEXT.value)
            }
            mAddressEdit?.apply {
                val add = pid?.address!!
                if(add > 0xFFFF) setText(add.toInt().toHex())
                    else setText(add.toShort().toHex())
                setTextColor(ColorList.TEXT.value)
            }
            mLengthEdit?.apply {
                setText(pid?.length.toString())
                setTextColor(ColorList.TEXT.value)
            }
            mGaugeMinEdit?.apply {
                setText(pid?.progMin.toString())
                setTextColor(ColorList.TEXT.value)
            }
            mGaugeMaxEdit?.apply {
                setText(pid?.progMax.toString())
                setTextColor(ColorList.TEXT.value)
            }
            mWarnMinEdit?.apply {
                setText(pid?.warnMin.toString())
                setTextColor(ColorList.TEXT.value)
            }
            mWarnMaxEdit?.apply {
                setText(pid?.warnMax.toString())
                setTextColor(ColorList.TEXT.value)
            }
            mEquationEdit?.apply {
                setText(pid?.equation)
                setTextColor(ColorList.TEXT.value)
            }
            mFormatEdit?.apply {
                setText(pid?.format)
                setTextColor(ColorList.TEXT.value)
            }
            mSmoothingEdit?.apply {
                setText(pid?.smoothing.toString())
                setTextColor(ColorList.TEXT.value)
            }
            mAssignToEdit?.apply {
                setText(pid?.assignTo)
                setTextColor(ColorList.TEXT.value)
            }
            mEnableSwitch?.apply {
                isChecked = pid?.enabled!!
                setTextColor(ColorList.TEXT.value)
            }
            mTabsEdit?.apply {
                setText(pid?.tabs)
                setTextColor(ColorList.TEXT.value)
            }
            mNameText?.setTextColor(ColorList.TEXT.value)
            mUnitText?.setTextColor(ColorList.TEXT.value)
            mAddressText?.setTextColor(ColorList.TEXT.value)
            mLengthText?.setTextColor(ColorList.TEXT.value)
            mGaugeMinText?.setTextColor(ColorList.TEXT.value)
            mGaugeMaxText?.setTextColor(ColorList.TEXT.value)
            mWarnMinText?.setTextColor(ColorList.TEXT.value)
            mWarnMaxText?.setTextColor(ColorList.TEXT.value)
            mEquationText?.setTextColor(ColorList.TEXT.value)
            mFormatText?.setTextColor(ColorList.TEXT.value)
            mSmoothingText?.setTextColor(ColorList.TEXT.value)
            mAssignToText?.setTextColor(ColorList.TEXT.value)
            mTabsText?.setTextColor(ColorList.TEXT.value)
            mDivider1?.setBackgroundColor(ColorList.BT_RIM.value)
        }

        fun saveData() {
            try {
                mPID?.let { pid ->
                    pid.name = mNameEdit?.text.toString()
                    pid.unit = mUnitEdit?.text.toString()
                    pid.address = parseLong(mAddressEdit?.text.toString().substringAfter("0x"), 16)
                    pid.length = mLengthEdit?.text.toString().toInt()
                    pid.progMin = mGaugeMinEdit?.text.toString().toFloat()
                    pid.progMax = mGaugeMaxEdit?.text.toString().toFloat()
                    pid.warnMin = mWarnMinEdit?.text.toString().toFloat()
                    pid.warnMax = mWarnMaxEdit?.text.toString().toFloat()
                    pid.equation = mEquationEdit?.text.toString()
                    pid.format = mFormatEdit?.text.toString()
                    pid.smoothing = mSmoothingEdit?.text.toString().toFloat()
                    pid.assignTo = mAssignToEdit?.text.toString()
                    pid.enabled = mEnableSwitch?.isChecked == true
                    pid.tabs = mTabsEdit?.text.toString()
                }
            } catch(e: Exception) {
                DebugLog.e(TAG, "D", e)
            }
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, mIndex)
        }

        init {
            mNameEdit = itemView.findViewById(R.id.editTextName)
            mNameText = itemView.findViewById(R.id.textViewName)
            mUnitEdit = itemView.findViewById(R.id.editTextUnit)
            mUnitText = itemView.findViewById(R.id.textViewUnit)
            mAddressEdit = itemView.findViewById(R.id.editTextAddress)
            mAddressText = itemView.findViewById(R.id.textViewAddress)
            mLengthEdit = itemView.findViewById(R.id.editTextLength)
            mLengthText = itemView.findViewById(R.id.textViewLength)
            mGaugeMinEdit = itemView.findViewById(R.id.editTextGaugeMin)
            mGaugeMinText = itemView.findViewById(R.id.textViewGaugeMin)
            mGaugeMaxEdit = itemView.findViewById(R.id.editTextGaugeMax)
            mGaugeMaxText = itemView.findViewById(R.id.textViewGaugeMax)
            mWarnMinEdit = itemView.findViewById(R.id.editTextWarnMin)
            mWarnMinText = itemView.findViewById(R.id.textViewWarnMin)
            mWarnMaxEdit = itemView.findViewById(R.id.editTextWarnMax)
            mWarnMaxText = itemView.findViewById(R.id.textViewWarnMax)
            mEquationEdit = itemView.findViewById(R.id.editTextEquation)
            mEquationText = itemView.findViewById(R.id.textViewEquation)
            mFormatEdit = itemView.findViewById(R.id.editTextFormat)
            mFormatText = itemView.findViewById(R.id.textViewFormat)
            mSmoothingEdit = itemView.findViewById(R.id.editTextSmoothing)
            mSmoothingText = itemView.findViewById(R.id.textViewSmoothing)
            mAssignToEdit = itemView.findViewById(R.id.editTextAssignTo)
            mAssignToText = itemView.findViewById(R.id.textViewAssignTo)
            mEnableSwitch = itemView.findViewById(R.id.switchEnable)
            mTabsText = itemView.findViewById(R.id.textViewTabs)
            mTabsEdit = itemView.findViewById(R.id.editTextTabs)
            mUpArrowButton = itemView.findViewById(R.id.buttonUp)
            mUpArrowButton?.apply {
                paintBG.color = ColorList.BT_BG.value
                paintRim.color = ColorList.BT_RIM.value
                setTextColor(ColorList.BT_TEXT.value)
                setOnClickListener {
                    if (mIndex > 0) {
                        onRowMoved(mIndex, mIndex - 1)
                    }
                }
            }
            mDownArrowButton = itemView.findViewById(R.id.buttonDown)
            mDownArrowButton?.apply {
                paintBG.color = ColorList.BT_BG.value
                paintRim.color = ColorList.BT_RIM.value
                setTextColor(ColorList.BT_TEXT.value)
                setOnClickListener {
                    if (mIndex < mData.count() - 1) {
                        onRowMoved(mIndex, mIndex + 1)
                    }
                }
            }
            mDivider1 = itemView.findViewById(R.id.divider1)
            mDivider1?.setBackgroundColor(ColorList.BT_RIM.value)
            itemView.setOnClickListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): PIDStruct? {
        return mData[id]
    }

    // convenience method for getting data at click position
    fun setItem(id: Int, data: PIDStruct?) {
        mData[id] = data
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    fun loadData() {
        mViews.forEach {
            it?.loadData()
        }
    }

    fun saveData() {
        mViews.forEach {
            it?.saveData()
        }
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int) {
            view?.invalidate()
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                val from = getItem(i)
                val to = getItem(i+1)
                setItem(i, to)
                setItem(i+1, from)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                val from = getItem(i)
                val to = getItem(i-1)
                setItem(i, to)
                setItem(i-1, from)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        notifyDataSetChanged()
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {
        myViewHolder?.saveData()
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
        myViewHolder?.loadData()
    }
}

class ItemMoveCallback(private val mAdapter: ItemTouchHelperContract) :
    ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {

    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mAdapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is SettingsViewAdapter.ViewHolder) {
                val myViewHolder: SettingsViewAdapter.ViewHolder = viewHolder
                mAdapter.onRowSelected(myViewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is SettingsViewAdapter.ViewHolder) {
            val myViewHolder: SettingsViewAdapter.ViewHolder = viewHolder
            mAdapter.onRowClear(myViewHolder)
        }
    }

    interface ItemTouchHelperContract {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(myViewHolder: SettingsViewAdapter.ViewHolder?)
        fun onRowClear(myViewHolder: SettingsViewAdapter.ViewHolder?)
    }
}