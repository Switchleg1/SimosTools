package com.app.simoslogger

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object Settings {
    var updateRate              = DEFAULT_UPDATE_RATE
    var outputDirectory         = DirectoryList.APP
    var invertCruise            = DEFAULT_INVERT_CRUISE
    var keepScreenOn            = DEFAULT_KEEP_SCREEN_ON
    var persistDelay            = DEFAULT_PERSIST_DELAY
    var persistQDelay           = DEFAULT_PERSIST_Q_DELAY
    var calculateHP             = DEFAULT_CALCULATE_HP
    var useMS2Torque            = DEFAULT_USE_MS2
    var tireDiameter            = DEFAULT_TIRE_DIAMETER
    var curbWeight              = DEFAULT_CURB_WEIGHT
    var dragCoefficient: Double = DEFAULT_DRAG_COEFFICIENT
    var alwaysPortrait          = DEFAULT_ALWAYS_PORTRAIT
    var displayType             = DisplayType.ROUND
    var drawMinMax              = DEFAULT_DRAW_MIN_MAX

    var msgList: Array<String>? = null
}