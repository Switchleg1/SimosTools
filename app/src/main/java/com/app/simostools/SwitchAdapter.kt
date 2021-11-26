package com.app.simostools

import android.content.Context
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class SwitchArrayAdapter(context: Context, textViewResourceId: Int, list: Array<String>) :
    ArrayAdapter<String>(context, textViewResourceId) {
    private val mContext: Context = context
    private val id: Int = textViewResourceId
    private var items: Array<String> = list

    override fun getView(position: Int, v: View?, parent: ViewGroup): View {
        var mView = v
        if (mView == null) {
            val vi = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mView = vi.inflate(id, null)
        }
        val text = mView!!.findViewById<TextView>(R.id.textView)

        text.setTextColor(ColorList.TEXT.value)
        text.text = items[position]

        return mView
    }

    override fun add(`object`: String?) {
        items += `object`?:""

        super.add(`object`)
    }
}