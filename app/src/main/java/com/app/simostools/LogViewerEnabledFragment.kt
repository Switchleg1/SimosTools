package com.app.simostools

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

class LogViewerEnabledViewModel : ViewModel() {
}

class LogViewerEnabledFragment: Fragment() {
    private var TAG                                                         = "LogViewerEnabledFragment"
    private var mEnableList:Array<androidx.appcompat.widget.SwitchCompat?>? = null
    private lateinit var mViewModel: LogViewerEnabledViewModel

    override fun onDestroy() {
        super.onDestroy()

        mEnableList = null

        DebugLog.d(TAG, "onDestroy")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_log_viewer_enabled, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(LogViewerEnabledViewModel::class.java)

        val setButton = view.findViewById<SwitchButton>(R.id.buttonOk)
        setButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                gLogViewerData?.let { playbackData ->
                    mEnableList?.let { enableList ->
                        playbackData.forEachIndexed() { i, pid ->
                            pid?.enabled = enableList[i]?.isChecked ?: false
                        }
                    }
                }
                findNavController().navigateUp()
            }
        }

        val backButton = view.findViewById<SwitchButton>(R.id.buttonBack)
        backButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigateUp()
            }
        }

        gLogViewerData?.let {
            mEnableList = arrayOfNulls(it.count())
            mEnableList?.let { enableList ->
                val lLayout = view.findViewById<LinearLayout>(R.id.logviewerLayoutScroll)
                it.forEachIndexed() { i, pid ->
                    val pidLayout = androidx.appcompat.widget.SwitchCompat(requireContext())
                    pidLayout.text = pid?.name
                    pidLayout.setTextColor(ColorList.TEXT.value)
                    pidLayout.textSize = 24f
                    pidLayout.isChecked = pid?.enabled ?: false
                    pidLayout.setTextColor(pid?.color?: Color.WHITE)
                    enableList[i] = pidLayout
                    lLayout.addView(pidLayout)
                }
            }
        }

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onResume() {
        super.onResume()

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

        DebugLog.d(TAG, "onPause")
    }

    override fun onStart() {
        super.onStart()

        DebugLog.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()

        DebugLog.d(TAG, "onStop")
    }
}