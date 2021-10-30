package com.app.simostools

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController

object ColorSettings {
    var mR = 255
    var mG = 255
    var mB = 255
    var mColorIndex = 0
    var mColorList = IntArray(ColorList.values().count())

    fun makeColor(): Int {
        return 0xFF000000.toInt() + ((mR and 0xFF) shl 16) + ((mG and 0xFF) shl 8) + (mB and 0xFF)
    }

    fun makeInverse(): Int {
        return Color.WHITE xor (makeColor() and 0xFFFFFF)
    }

    fun resetColors() {
        for(i in 0 until mColorList.count())
            mColorList[i] = ColorList.values()[i].value
    }

    fun getColor(index: ColorList) {
        mColorIndex = index.ordinal
        val c = mColorList[mColorIndex]
        mR = (c and 0xFF0000) shr 16
        mG = (c and 0xFF00) shr 8
        mB = c and 0xFF
    }

    fun setColor() {
        mColorList[mColorIndex] = Color.rgb(mR, mG, mB)
    }
}

class ColorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_color, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonCancelColor).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<Button>(R.id.buttonSaveColor).setOnClickListener {
            doSave()
            findNavController().navigateUp()
        }

        //setup seek bars
        val rSeek = view.findViewById<SeekBar>(R.id.seekBarColorR)
        val gSeek = view.findViewById<SeekBar>(R.id.seekBarColorG)
        val bSeek = view.findViewById<SeekBar>(R.id.seekBarColorB)
        rSeek.max = 255
        gSeek.max = 255
        bSeek.max = 255
        rSeek.progress = ColorSettings.mR
        gSeek.progress = ColorSettings.mG
        bSeek.progress = ColorSettings.mB
        doSetColor()

        view.findViewById<SeekBar>(R.id.seekBarColorR).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                doSetColor()
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarColorG).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                doSetColor()
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarColorB).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                doSetColor()
            }
        })

        //Set color picker
        val iColor = view.findViewById<ImageView>(R.id.imageViewColor)
        iColor.setBackgroundColor(Color.rgb(ColorSettings.mR, ColorSettings.mG, ColorSettings.mB))

        //Set colors
        view.setBackgroundColor(ColorList.BG_NORMAL.value)
        view.findViewById<TextView>(R.id.textViewColorR).setTextColor(ColorList.TEXT.value)
        view.findViewById<TextView>(R.id.textViewColorG).setTextColor(ColorList.TEXT.value)
        view.findViewById<TextView>(R.id.textViewColorB).setTextColor(ColorList.TEXT.value)
    }

    private fun doSave() {
        ColorSettings.setColor()
    }

    private fun doSetColor() {
        view?.let { currentView ->
            val rSeek = currentView.findViewById<SeekBar>(R.id.seekBarColorR)
            val gSeek = currentView.findViewById<SeekBar>(R.id.seekBarColorG)
            val bSeek = currentView.findViewById<SeekBar>(R.id.seekBarColorB)
            val rText = currentView.findViewById<TextView>(R.id.textViewColorR)
            val gText = currentView.findViewById<TextView>(R.id.textViewColorG)
            val bText = currentView.findViewById<TextView>(R.id.textViewColorB)
            val iColor = currentView.findViewById<ImageView>(R.id.imageViewColor)

            //Get colors from seek bars
            ColorSettings.mR = rSeek.progress
            ColorSettings.mG = gSeek.progress
            ColorSettings.mB = bSeek.progress

            //set text
            rText.text = getString(R.string.textview_color_r, rSeek.progress)
            gText.text = getString(R.string.textview_color_g, gSeek.progress)
            bText.text = getString(R.string.textview_color_b, bSeek.progress)

            //update color
            iColor.setBackgroundColor(ColorSettings.makeColor())
        }
    }
}