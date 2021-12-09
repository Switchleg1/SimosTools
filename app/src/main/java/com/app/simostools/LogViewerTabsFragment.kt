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

class LogViewerTabsViewModel : ViewModel() {
}

class LogViewerTabsFragment: Fragment() {
    private var TAG                                                         = "LogViewerLayoutFragment"
    private var mTabsList:Array<androidx.appcompat.widget.SwitchCompat?>? = null
    private lateinit var mViewModel: LogViewerTabsViewModel

    override fun onDestroy() {
        super.onDestroy()

        mTabsList = null

        DebugLog.d(TAG, "onDestroy")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_log_viewer_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(LogViewerTabsViewModel::class.java)

        val setButton = view.findViewById<SwitchButton>(R.id.buttonOk)
        setButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                gLogViewerData?.let { playbackData ->
                    mTabsList?.let { tabsList ->
                        playbackData.forEachIndexed() { i, pid ->
                            pid?.let {
                                pid.enabled = false
                                tabsList.forEachIndexed() { l, layout ->
                                    layout?.let {
                                        if (layout.isChecked && pid.tabs.contains(layout.text))
                                            pid.enabled = true
                                    }
                                }
                            }
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

        gLogViewerData?.let { playbackData ->
            val lLayout = view.findViewById<LinearLayout>(R.id.logviewerLayoutScroll)
            var tabStrings:Array<String> = arrayOf()
            tabStrings += "Default"
            playbackData.forEachIndexed() { i, pid ->
                pid?.tabs?.split(".")?.forEach { tab ->
                    val actualTab = tab.substringBefore("|")
                    if (actualTab.isNotEmpty() && tabStrings.find { it == actualTab } == null && actualTab != "DSG") {
                        DebugLog.d(TAG, "New Tab: $actualTab")
                        tabStrings += actualTab
                    }
                }
            }
            tabStrings += "DSG"

            mTabsList = arrayOfNulls(tabStrings.count())
            mTabsList?.let { enableList ->
                tabStrings.forEachIndexed() { i, tab ->
                    val pidLayout = androidx.appcompat.widget.SwitchCompat(requireContext())
                    pidLayout.text = tab
                    pidLayout.setTextColor(ColorList.TEXT.value)
                    pidLayout.textSize = 24f
                    pidLayout.isChecked = false
                    pidLayout.setTextColor(Color.WHITE)
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