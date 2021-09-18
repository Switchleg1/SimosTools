package com.app.vwflashtools

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

fun writeDefaultConfig(filename: String?, context: Context?) {
    ConfigFile.put("PID0.Address", "f40C")
    ConfigFile.put("PID0.Length", "2")
    ConfigFile.put("PID0.Equation", "6")
    ConfigFile.put("PID0.Name", "Engine Speed")
    ConfigFile.put("PID0.Unit", "rpm")
    ConfigFile.put("PID0.Format", "%04.2f")
    ConfigFile.put("PID0.Min", "0")
    ConfigFile.put("PID0.Max", "7000")
    ConfigFile.put("PID0.WarnMin", "-1")
    ConfigFile.put("PID0.WarnMax", "6000")

    ConfigFile.put("PID1.Address", "15d3")
    ConfigFile.put("PID1.Length", "2")
    ConfigFile.put("PID1.Equation", "5")
    ConfigFile.put("PID1.Name", "Speed")
    ConfigFile.put("PID1.Unit", "km/h")
    ConfigFile.put("PID1.Format", "%03.2f")
    ConfigFile.put("PID1.Min", "0")
    ConfigFile.put("PID1.Max", "220")
    ConfigFile.put("PID1.WarnMin", "-20")
    ConfigFile.put("PID1.WarnMax", "220")

    ConfigFile.put("PID2.Address", "39c0")
    ConfigFile.put("PID2.Length", "2")
    ConfigFile.put("PID2.Equation", "10")
    ConfigFile.put("PID2.Name", "MAP Actual")
    ConfigFile.put("PID2.Unit", "bar")
    ConfigFile.put("PID2.Format", "%02.3f")
    ConfigFile.put("PID2.Min", "0")
    ConfigFile.put("PID2.Max", "3")
    ConfigFile.put("PID2.WarnMin", "-0.1")
    ConfigFile.put("PID2.WarnMax", "2.7")

    ConfigFile.put("PID3.Address", "39c2")
    ConfigFile.put("PID3.Length", "2")
    ConfigFile.put("PID3.Equation", "10")
    ConfigFile.put("PID3.Name", "PUT Actual")
    ConfigFile.put("PID3.Unit", "bar")
    ConfigFile.put("PID3.Format", "%02.3f")
    ConfigFile.put("PID3.Min", "0")
    ConfigFile.put("PID3.Max", "3")
    ConfigFile.put("PID3.WarnMin", "-0.1")
    ConfigFile.put("PID3.WarnMax", "2.7")

    ConfigFile.put("PID4.Address", "3d97")
    ConfigFile.put("PID4.Length", "2")
    ConfigFile.put("PID4.Equation", "9")
    ConfigFile.put("PID4.Name", "Lambda SAE")
    ConfigFile.put("PID4.Unit", "bar")
    ConfigFile.put("PID4.Format", "%01.3f")
    ConfigFile.put("PID4.Min", "0")
    ConfigFile.put("PID4.Max", "2")
    ConfigFile.put("PID4.WarnMin", "0.5")
    ConfigFile.put("PID4.WarnMax", "3.0")

    ConfigFile.put("PID5.Address", "206d")
    ConfigFile.put("PID5.Length", "2")
    ConfigFile.put("PID5.Equation", "9")
    ConfigFile.put("PID5.Name", "Throttle")
    ConfigFile.put("PID5.Unit", "%")
    ConfigFile.put("PID5.Format", "%03.2f")
    ConfigFile.put("PID5.Min", "0")
    ConfigFile.put("PID5.WarnMin", "-1")
    ConfigFile.put("PID5.WarnMax", "101")

    ConfigFile.put("PID6.Address", "1040")
    ConfigFile.put("PID6.Length", "2")
    ConfigFile.put("PID6.Equation", "4")
    ConfigFile.put("PID6.Name", "Turbo Speed")
    ConfigFile.put("PID6.Unit", "rpm")
    ConfigFile.put("PID6.Format", "%06.2f")
    ConfigFile.put("PID6.Min", "0")
    ConfigFile.put("PID6.Max", "190000")
    ConfigFile.put("PID6.WarnMin", "-10")
    ConfigFile.put("PID6.WarnMax", "185000")

    ConfigFile.put("PID7.Address", "f406")
    ConfigFile.put("PID7.Length", "1")
    ConfigFile.put("PID7.Equation", "3")
    ConfigFile.put("PID7.Name", "STFT")
    ConfigFile.put("PID7.Unit", "%")
    ConfigFile.put("PID7.Format", "%03.2f")
    ConfigFile.put("PID7.Min", "-25")
    ConfigFile.put("PID7.Max", "25")
    ConfigFile.put("PID7.WarnMin", "-20")
    ConfigFile.put("PID7.WarnMax", "20")

    ConfigFile.put("PID8.Address", "1001")
    ConfigFile.put("PID8.Length", "1")
    ConfigFile.put("PID8.Equation", "7")
    ConfigFile.put("PID8.Name", "IAT")
    ConfigFile.put("PID8.Unit", "°C")
    ConfigFile.put("PID8.Format", "%03.2f")
    ConfigFile.put("PID8.Min", "0")
    ConfigFile.put("PID8.Max", "70")
    ConfigFile.put("PID8.WarnMin", "-40")
    ConfigFile.put("PID8.WarnMax", "55")

    ConfigFile.put("PID9.Address", "13f2")
    ConfigFile.put("PID9.Length", "1")
    ConfigFile.put("PID9.Equation", "1")
    ConfigFile.put("PID9.Name", "Retard cylinder 1")
    ConfigFile.put("PID9.Unit", "°")
    ConfigFile.put("PID9.Format", "%02.2f")
    ConfigFile.put("PID9.Min", "0")
    ConfigFile.put("PID9.Max", "10")
    ConfigFile.put("PID9.WarnMin", "-10")
    ConfigFile.put("PID9.WarnMax", "4")

    ConfigFile.put("PID10.Address", "13f3")
    ConfigFile.put("PID10.Length", "1")
    ConfigFile.put("PID10.Equation", "1")
    ConfigFile.put("PID10.Name", "Retard cylinder 2")
    ConfigFile.put("PID10.Unit", "°")
    ConfigFile.put("PID10.Format", "%02.2f")
    ConfigFile.put("PID10.Min", "0")
    ConfigFile.put("PID10.Max", "10")
    ConfigFile.put("PID10.WarnMin", "-10")
    ConfigFile.put("PID10.WarnMax", "4")

    ConfigFile.put("PID11.Address", "13f4")
    ConfigFile.put("PID11.Length", "1")
    ConfigFile.put("PID11.Equation", "1")
    ConfigFile.put("PID11.Name", "Retard cylinder 3")
    ConfigFile.put("PID11.Unit", "°")
    ConfigFile.put("PID11.Format", "%02.2f")
    ConfigFile.put("PID11.Min", "0")
    ConfigFile.put("PID11.Max", "10")
    ConfigFile.put("PID11.WarnMin", "-10")
    ConfigFile.put("PID11.WarnMax", "4")

    ConfigFile.put("PID12.Address", "13f5")
    ConfigFile.put("PID12.Length", "1")
    ConfigFile.put("PID12.Equation", "1")
    ConfigFile.put("PID12.Name", "Retard cylinder 4")
    ConfigFile.put("PID12.Unit", "°")
    ConfigFile.put("PID12.Format", "%02.2f")
    ConfigFile.put("PID12.Min", "0")
    ConfigFile.put("PID12.Max", "10")
    ConfigFile.put("PID12.WarnMin", "-10")
    ConfigFile.put("PID12.WarnMax", "4")

    ConfigFile.put("PID13.Address", "2025")
    ConfigFile.put("PID13.Length", "2")
    ConfigFile.put("PID13.Equation", "10")
    ConfigFile.put("PID13.Name", "LFP Actual")
    ConfigFile.put("PID13.Unit", "bar")
    ConfigFile.put("PID13.Format", "%03.2f")
    ConfigFile.put("PID13.Min", "0")
    ConfigFile.put("PID13.Max", "15")
    ConfigFile.put("PID13.WarnMin", "5")
    ConfigFile.put("PID13.WarnMax", "15")

    ConfigFile.put("PID14.Address", "2027")
    ConfigFile.put("PID14.Length", "2")
    ConfigFile.put("PID14.Equation", "8")
    ConfigFile.put("PID14.Name", "HFP Actual")
    ConfigFile.put("PID14.Unit", "bar")
    ConfigFile.put("PID14.Format", "%03.2f")
    ConfigFile.put("PID14.Min", "0")
    ConfigFile.put("PID14.Max", "250")
    ConfigFile.put("PID14.WarnMin", "50")
    ConfigFile.put("PID14.WarnMax", "250")

    ConfigFile.put("PID15.Address", "203c")
    ConfigFile.put("PID15.Length", "2")
    ConfigFile.put("PID15.Equation", "0")
    ConfigFile.put("PID15.Name", "Cruise control status")
    ConfigFile.put("PID15.Unit", "")
    ConfigFile.put("PID15.Format", "%03.2f")
    ConfigFile.put("PID15.Min", "0")
    ConfigFile.put("PID15.Max", "2")
    ConfigFile.put("PID15.WarnMin", "-1")
    ConfigFile.put("PID15.WarnMax", "3")

    ConfigFile.put("PID16.Address", "203f")
    ConfigFile.put("PID16.Length", "2")
    ConfigFile.put("PID16.Equation", "8")
    ConfigFile.put("PID16.Name", "Torque limit")
    ConfigFile.put("PID16.Unit", "Nm")
    ConfigFile.put("PID16.Format", "%04.2f")
    ConfigFile.put("PID16.Min", "0")
    ConfigFile.put("PID16.Max", "450")
    ConfigFile.put("PID16.WarnMin", "-1")
    ConfigFile.put("PID16.WarnMax", "600")

    ConfigFile.put("PID17.Address", "202f")
    ConfigFile.put("PID17.Length", "2")
    ConfigFile.put("PID17.Equation", "2")
    ConfigFile.put("PID17.Name", "Oil temp")
    ConfigFile.put("PID17.Unit", "°C")
    ConfigFile.put("PID17.Format", "%03.2f")
    ConfigFile.put("PID17.Min", "0")
    ConfigFile.put("PID17.Max", "450")
    ConfigFile.put("PID17.WarnMin", "-1")
    ConfigFile.put("PID17.WarnMax", "600")

    ConfigFile.put("PID18.Address", "f456")
    ConfigFile.put("PID18.Length", "1")
    ConfigFile.put("PID18.Equation", "3")
    ConfigFile.put("PID18.Name", "LTFT")
    ConfigFile.put("PID18.Unit", "%")
    ConfigFile.put("PID18.Format", "%02.2f")
    ConfigFile.put("PID18.Min", "-25")
    ConfigFile.put("PID18.Max", "25")
    ConfigFile.put("PID18.WarnMin", "-20")
    ConfigFile.put("PID18.WarnMax", "20")

    ConfigFile.put("PID19.Address", "39a9")
    ConfigFile.put("PID19.Length", "2")
    ConfigFile.put("PID19.Equation", "9")
    ConfigFile.put("PID19.Name", "Ignition angle")
    ConfigFile.put("PID19.Unit", "°")
    ConfigFile.put("PID19.Format", "%03.2f")
    ConfigFile.put("PID19.Min", "-10")
    ConfigFile.put("PID19.Max", "25")
    ConfigFile.put("PID19.WarnMin", "-6")
    ConfigFile.put("PID19.WarnMax", "25")

    ConfigFile.put("PID20.Address", "3e0a")
    ConfigFile.put("PID20.Length", "2")
    ConfigFile.put("PID20.Equation", "9")
    ConfigFile.put("PID20.Name", "Coolant temp")
    ConfigFile.put("PID20.Unit", "°C")
    ConfigFile.put("PID20.Format", "%03.2f")
    ConfigFile.put("PID20.Min", "-50")
    ConfigFile.put("PID20.Max", "140")
    ConfigFile.put("PID20.WarnMin", "-50")
    ConfigFile.put("PID20.WarnMax", "130")

    ConfigFile.put("PID21.Address", "295c")
    ConfigFile.put("PID21.Length", "1")
    ConfigFile.put("PID21.Equation", "0")
    ConfigFile.put("PID21.Name", "Flaps Actual")
    ConfigFile.put("PID21.Unit", "")
    ConfigFile.put("PID21.Format", "%01.0f")
    ConfigFile.put("PID21.Min", "0")
    ConfigFile.put("PID21.Max", "1")
    ConfigFile.put("PID21.WarnMin", "-1")
    ConfigFile.put("PID21.WarnMax", "2")

    ConfigFile.put("PID22.Address", "2932")
    ConfigFile.put("PID22.Length", "2")
    ConfigFile.put("PID22.Equation", "8")
    ConfigFile.put("PID22.Name", "LFP Command")
    ConfigFile.put("PID22.Unit", "bar")
    ConfigFile.put("PID22.Format", "%02.2f")
    ConfigFile.put("PID22.Min", "0")
    ConfigFile.put("PID22.Max", "15")
    ConfigFile.put("PID22.WarnMin", "5")
    ConfigFile.put("PID22.WarnMax", "15")

    ConfigFile.put("PID23.Address", "293b")
    ConfigFile.put("PID23.Length", "2")
    ConfigFile.put("PID23.Equation", "10")
    ConfigFile.put("PID23.Name", "HFP Command")
    ConfigFile.put("PID23.Unit", "bar")
    ConfigFile.put("PID23.Format", "%03.2f")
    ConfigFile.put("PID23.Min", "0")
    ConfigFile.put("PID23.Max", "250")
    ConfigFile.put("PID23.WarnMin", "5")
    ConfigFile.put("PID23.WarnMax", "250")

    ConfigFile.put("PIDX.Position", "15")

    ConfigFile.write(filename, context)
}

data class CFGKey(val key: String, val value: String)

object ConfigFile {
    private val TAG = "Config"
    private val mProperties = Properties()
    private var mKeys: Array<CFGKey?> = arrayOf()

    fun write(fileName: String?, context: Context?) {
        val path = context?.getExternalFilesDir("")
        val propertiesFile = File(path, "/$fileName")
        propertiesFile.createNewFile()
        val propertiesOutputStream = FileOutputStream(propertiesFile)
        mProperties.store(propertiesOutputStream, "save to properties file")
    }

    fun put(key: String, value: String) {
        mProperties[key] = value
    }

    fun read(fileName: String?, context: Context?) {
        val path = context?.getExternalFilesDir("")
        val propertiesFile = File(path, "/$fileName")
        val inputStream = FileInputStream(propertiesFile)
        mProperties.load(inputStream)

        mKeys = arrayOf()
        mProperties.forEach{(k, v) ->
            mKeys = mKeys.copyOf(mKeys.size + 1)
            mKeys[mKeys.size] = CFGKey((k as String), (v as String))
        }
    }

    fun get(key: String): String {
        try {
            for (i in 0 until mKeys.count()) {
                if (mKeys[i]!!.key == key) {
                    return mKeys[i]!!.value
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Exception during read", e)
        }
        return ""
    }

    fun get(index: Int): String {
        if(mKeys[index] != null)
            return mKeys[index]!!.key

        return ""
    }

    fun count(): Int {
        return mKeys.count()
    }
}