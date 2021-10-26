package com.app.simoslogger

import android.content.Context
import java.lang.Exception

data class PIDStruct(var address: Long,
                     var length: Int,
                     var signed: Boolean,
                     var progMin: Float,
                     var progMax: Float,
                     var warnMin: Float,
                     var warnMax: Float,
                     var smoothing: Float,
                     var value: Float,
                     var equation: String,
                     var format: String,
                     var name: String,
                     var unit: String,
                     var enabled: Boolean,
                     var tabs: String)

data class DATAStruct(var min: Float,
                      var max: Float,
                      var warn: Boolean,
                      var multiplier: Float,
                      var inverted: Boolean)

object PIDs {
    private val TAG         = "PIDs"

    var list22: Array<PIDStruct?>? = arrayOf(
        PIDStruct(0x2033, 2, false,0f,  220f,   -20f, 200f,   0.0f, 0f,"x / 347.947",           "%05.1f","Vehicle Speed",       "km/hr",  true, ""),
        PIDStruct(0xf40C, 2, false,0f,  7000f,  -1f,  6000f,  0.0f, 0f,"x / 4",                 "%05.1f","Engine Speed",        "rpm",    true, ""),
        PIDStruct(0x39c0, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Actual",          "kpa",    true, ""),
        PIDStruct(0x39c1, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Setpoint",        "kpa",    true, ""),
        PIDStruct(0x39c2, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","PUT Actual",          "kpa",    true, ""),
        PIDStruct(0x10c0, 2, false,0f,  2f,     0.7f, 5f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Actual",       "l",      true, ""),
        PIDStruct(0x2950, 2, false,0f,  2f,     0.0f, 5f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Setpoint",     "l",      true, ""),
        PIDStruct(0x39a2, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Actual",    "%",      true, ""),
        PIDStruct(0x39a3, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Setpoint",  "%",      true, ""),
        PIDStruct(0x2032, 2, false,0f,  1500f,  -1f,  1500f,  0.0f, 0f,"x / 2.4",               "%04.2f","Airmass Actual",      "g/s",    true, ""),
        PIDStruct(0x13a0, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 DI","ms",     true, ""),
        PIDStruct(0x13a4, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 PI","ms",     true, ""),
        PIDStruct(0x13a8, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 MP","ms",     true, ""),
        PIDStruct(0x437C, 2, true, -50f,450f,   -100f,500f,   0.0f, 0f,"x / 10",                "%03.2f","Torque Actual",       "Nm",     true, ""),
        PIDStruct(0x4380, 2, true, 0f,  500f,   -100f,500f,   0.7f, 0f,"x / 10",                "%04.1f","Torque Requested",    "Nm",     true, ""),
        PIDStruct(0x2027, 2, false,0f,  28000f, -1f,  28000f, 0.0f, 0f,"x * 10",                "%04.1f","HPFP Actual",         "kpa",    true, ""),
        PIDStruct(0x293b, 2, false,0f,  25000f, 600f, 25000f, 0.0f, 0f,"x * 10",                "%05.0f","HPFP Setpoint",       "kpa",    true, ""),
        PIDStruct(0x209a, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"x / 100",               "%04.1f","HPFP Volume",         "%",      true, ""),
        PIDStruct(0x2025, 2, false,0f,  1500f,  600f, 1500f,  0.7f, 0f,"x / 10",                "%03.0f","LPFP Actual",         "kpa",    true, ""),
        PIDStruct(0x2028, 2, false,0f,  100f,   -1.0f,100f,   0.7f, 0f,"x / 100",               "%04.1f","LPFP Duty",           "%",      true, ""),
        PIDStruct(0x293b, 2, false,0f,  25000f, 600f, 25000f, 0.0f, 0f,"x / 10",                "%05.0f","LPFP Setpoint",       "kpa",    true, ""),
        PIDStruct(0xf456, 1, false,-25f,25f,    -20f, 20f,    0.7f, 0f,"x / 1.28 - 100",        "%04.1f","Fuel Trim Long Term", "%",      true, ""),
        PIDStruct(0xf406, 1, false,-25f,25f,    -20f, 20f,    0.0f, 0f,"x / 1.28 - 100",        "%04.1f","Fuel Trim Short Term","%",      true, ""),
        PIDStruct(0x20ba, 2, true, 0f,  100f,   -1f,  101f,   0.0f, 0f,"x / 10",                "%04.1f","Throttle Sensor",     "%",      true, ""),
        PIDStruct(0x1040, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x * 6.1035",            "%06.0f","Turbo Speed",         "rpm",    true, ""),
        PIDStruct(0x2004, 2, true, -10f,10f,    -15f, 60f,    0.0f, 0f,"x / 100",               "%04.2f","Ignition Actual",     "°",      true, ""),
        PIDStruct(0x200a, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 1",   "°",      true, ""),
        PIDStruct(0x200b, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 2",   "°",      true, ""),
        PIDStruct(0x200c, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 3",   "°",      true, ""),
        PIDStruct(0x200d, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 4",   "°",      true, ""),
        PIDStruct(0x1001, 1, false,-40f,55f,    -35f, 50f,    0.7f, 0f,"x * 0.75 - 48",         "%03.1f","Intake Air Temp",     "°C",     true, ""),
        PIDStruct(0x1041, 2, true, -40f,55f,    -500f,500f,   0.7f, 0f,"x * 0.005859375 + 144", "%03.1f","Turbo Air Temp",      "°C",     true, ""),
        PIDStruct(0x295c, 1, false,0f,  1f,     -1f,  2f,     0.0f, 0f,"x",                     "%01.0f","Flaps Actual",        "",       true, ""),
        PIDStruct(0x295d, 1, false,0f,  1f,     -1f,  2f,     0.0f, 0f,"x",                     "%01.0f","Flaps Setpoint",      "",       true, ""),
        PIDStruct(0x2904, 2, false,0f,  20f,    -1f,  10f,    0.0f, 0f,"x",                     "%02.0f","Misfire Sum Global",  "",       true, ""),
        PIDStruct(0x202f, 2, false,-50f,130f,   0f,   120f,   0.9f, 0f,"(x - 2731.4) / 10",     "%04.1f","Oil Temp",            "°C",     true, ""),
        PIDStruct(0x13ca, 2, false,50f, 120f,   70f,  120.0f, 0.9f, 0f,"x / 120.60176665439",   "%03.0f","Ambient Pressure",    "kpa",    true, ""),
        PIDStruct(0x1004, 2, true, -40f,50f,    -30f, 50f,    0.7f, 0f,"x / 128",               "%05.2f","Ambient Air Temp",    "°C",     true, ""),
        PIDStruct(0x101e, 1, false,0f,  100f,   -1f,  110f,   0.0f, 0f,"x * 0.390625",          "%03.1f","Cooling Fan",         "%",      true, ""),
        PIDStruct(0x14ec, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"x * 0.09765625",        "%03.1f","CPU Load",            "%",      true, ""),
        PIDStruct(0x167c, 1, false,0f,  1f,     -1f,  100f,   0.0f, 0f,"x",                     "%03.1f","Valve Lift Position", "",       true, ""),
        PIDStruct(0x201a, 2, true, -10f,10f,    -50f, 50f,    0.0f, 0f,"x / 10",                "%03.1f","Exhaust Cam Position","°",      true, ""),
        PIDStruct(0x201e, 2, true, -10f,10f,    -50f, 50f,    0.0f, 0f,"x / 10",                "%03.1f","Intake Cam Position", "°",      true, ""),
        PIDStruct(0x210f, 2, false,0f,  6f,     -1f,  10f,    0.0f, 0f,"x",                     "%01.0f","Gear",                "",       true, ""),
        PIDStruct(0x11cd, 1, false,-50f,120f,   -100f,120f,   0.0f, 0f,"x - 40",                "%02.0f","Coolant Temp",        "°C",     true, ""),
        PIDStruct(0x14a6, 1, false,0f,  2f,     8f,   16f,    0.8f, 0f,"x * 0.1015625",         "%01.0f","Battery Voltage",     "V",      true, ""),
        PIDStruct(0x203c, 2, false,0f,  1f,     -1f,  100f,   0.0f, 0f,"x",                     "%01.0f","Cruise Control",      "",       true, ""),
    )

    var list3E: Array<PIDStruct?>? = arrayOf(
        PIDStruct(0xd000ee2a, 1, false,-10f, 10f,    -1000f,1000f,  0.8f, 0f,"(x - 127) / 10",    "%05.3f","Accel. Lat",          "m/s2",  true, ""),
        PIDStruct(0xd00141ba, 2, false,-10f, 10f,    -1000f,1000f,  0.4f, 0f,"(x - 512) / 32",    "%05.3f","Accel. Long",         "m/s2",  true, ""),
        PIDStruct(0xd00097b4, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%05.3f","Airmass Actual",      "g/stk", true, ""),
        PIDStruct(0xd00097fc, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%05.3f","Airmass Setpoint",    "g/stk", true, ""),
        PIDStruct(0xd000c177, 1, false,-25f, 45f,    -100f, 100f,   0.9f, 0f,"(x - 64) / 1.33",   "%03.2f","Ambient Air Temp",    "°C",    true, ""),
        PIDStruct(0xd0013c76, 2, false,0f,   110f,   0f,    120f,   0.9f, 0f,"x / 120.60176665",  "%03.2f","Ambient Pressure",    "kpa",   true, ""),
        PIDStruct(0xd0015172, 2, true, 10f,  15f,    7f,    16f,    0.7f, 0f,"x / 51.2",          "%04.1f","Battery Volts",       "V",     true, ""),
        PIDStruct(0xd000c36e, 1, false,-10f, 10f,    -100f, 100f,   0.0f, 0f,"x",                 "%05.2f","Combustion Mode",     "",      true, ""),
        PIDStruct(0xd000c6f5, 1, false,-50f, 130f,   -100f, 150f,   0.8f, 0f,"(x - 64) / 1.33",   "%03.0f","Coolant Temp",        "°C",    true, ""),
        PIDStruct(0xd001397a, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Limit",           "°",     true, ""),
        PIDStruct(0xd0013982, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Actual",          "°",     true, ""),
        PIDStruct(0xd0012400, 2, false,0f,   7000f,  -1.0f, 6000f,  0.0f, 0f,"x",                 "%04.0f","Engine Speed",        "rpm",   true, ""),
        PIDStruct(0xd000c1d4, 1, false,-100f,100f,   -100f, 100f,   0.0f, 0f,"x / 2.55",          "%01.0f","Ethanol Content",     "%",     true, ""),
        PIDStruct(0xd0011e04, 2, false,0f,   1.5f,   -1f,   10f,    0.0f, 0f,"x / 16384",         "%03.2f","Exhaust Flow Factor", "",      true, ""),
        PIDStruct(0xd001566e, 2, true, -45f, 45f,    -100f, 100f,   0.0f, 0f,"x / 128",           "%04.1f","Exhaust Cam Position","°",     true, ""),
        PIDStruct(0xd0011eba, 2, false,0f,   500f,   -100f, 1000f,  0.0f, 0f,"x / 120.60176665",  "%03.0f","Exhaust Pres Desired","kpa",   true, ""),
        PIDStruct(0xd00135e0, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow Desired",   "mg/stk",true, ""),
        PIDStruct(0xd0013636, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow",           "mg/stk",true, ""),
        PIDStruct(0xd00192b1, 1, false,-100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Fuel Flow Split MPI", "",      true, ""),
        PIDStruct(0xd0013600, 2, false,0f,   100f,   -1000f,100f,   0.0f, 0f,"x / 655.35999999",  "%01.0f","Fuel LPFP Duty",      "%",     true, ""),
        PIDStruct(0xd0011b26, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%04.0f","Fuel LPFR Actual",    "kpa",   true, ""),
        PIDStruct(0xd001360c, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%04.0f","Fuel LPFR Setpoint",  "kpa",   true, ""),
        PIDStruct(0xd00136ac, 2, false,0f,   28000f, 0f,    28000f, 0.0f, 0f,"x / 1.884402603",   "%05.0f","Fuel HPFR Actual",    "kpa",   true, ""),
        PIDStruct(0xd0013640, 2, false,0f,   28000f, -1000f,30000f, 0.0f, 0f,"x / 1.8844026039",  "%05.0f","Fuel HPFR Setpoint",  "kpa",   true, ""),
        PIDStruct(0xd001363c, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999999",  "%01.0f","Fuel HPFP Volume",    "%",     true, ""),
        PIDStruct(0xd000f00b, 1, false,-25f, 25f,    -20f,  20f,    0.8f, 0f,"x / 1.28 - 100",    "%01.0f","Fuel Trim Long Term", "%",     true, ""),
        PIDStruct(0xd000f00c, 1, false,-25f, 25f,    -20f,  20f,    0.7f, 0f,"x / 1.28 - 100.0",  "%04.1f","Fuel Trim Short Term","%",     true, ""),
        PIDStruct(0xd000f39a, 1, false,0f,   6f,     -1f,   7f,     0.0f, 0f,"x",                 "%02.2f","Gear",                "gear",  true, ""),
        PIDStruct(0xd000e57e, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95) / 2.66667","%01.0f","Ignition Table Value","°",     true, ""),
        PIDStruct(0xd000e59c, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95) / 2.66667","%01.0f","Ignition Timing",     "°",     true, ""),
        PIDStruct(0xd001566c, 2, true, -100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Intake Cam Position", "°",     true, ""),
        PIDStruct(0xd0013b16, 2, false,-25f, 25f,    -1000f,1000f,  0.0f, 0f,"x / 250",           "%02.2f","Injector PW DI",      "ms",    true, ""),
        PIDStruct(0xd0013824, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 250",           "%05.1f","Injector PW MPI",     "ms",    true, ""),
        PIDStruct(0xd000c179, 1, false,-50f, 70f,    -20f,  50f,    0.9f, 0f,"(x - 64) / 1.33",   "%03.2f","Intake Air Temp",     "°C",    true, ""),
        PIDStruct(0xd0011e08, 2, false,0f,   20f,    -1000f,1000f,  0.0f, 0f,"x / 16384",         "%03.0f","Intake Flow Factor",  "-",     true, ""),
        PIDStruct(0xd001988e, 1, false,0f,   -5f,    -3.0f, 3f,     0.8f, 0f,"(x - 128) / 2.66",  "%05.3f","Knock Retard",        "°",     true, ""),
        PIDStruct(0xd000efb1, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 1",  "°",     true, ""),
        PIDStruct(0xd000efb2, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 2",  "°",     true, ""),
        PIDStruct(0xd000efb3, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 3",  "°",     true, ""),
        PIDStruct(0xd000efb4, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 4",  "°",     true, ""),
        PIDStruct(0xd00120e2, 2, false,0.5f, 1.5f,   -0.1f, 5f,     0.0f, 0f,"x / 32767.999999",  "%04.2f","Lambda Actual",       "l",     true, ""),
        PIDStruct(0xd00143f6, 2, false,0f,   2f,     -100f, 500f,   0.0f, 0f,"x / 1024",          "%03.2f","Lambda Setpoint",     "l",     true, ""),
        PIDStruct(0xd00098cc, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000",          "%05.1f","MAP Actual",          "kpa",   true, ""),
        PIDStruct(0xd00098f4, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000",          "%05.1f","MAP Setpoint",        "kpa",   true, ""),
        PIDStruct(0xd000c5ae, 1, false,-25f, 120f,   -1000f,1000f,  0.9f, 0f,"x - 40",            "%01.0f","Oil Temp",            "°C",    true, ""),
        PIDStruct(0xd000e578, 1, true, 0f,   220f,   -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.6667","%03.2f","openflex_cor",        "°CRK",  true, ""),
        PIDStruct(0xd001de8d, 1, true, 0f,   7000f,  -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.6667","%06.1f","openflex_max_cor",    "°CRK",  true, ""),
        PIDStruct(0xd001de8e, 1, false,0f,   3f,     -1000f,1000f,  0.0f, 0f,"x / 128",           "%05.3f","openflex_fac_cor",    "",      true, ""),
        PIDStruct(0xd0012028, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 10.24",         "%04.1f","Pedal Position",      "%",     true, ""),
        PIDStruct(0xd0000aa1, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Port Flap Position",  "",      true, ""),
        PIDStruct(0xd00098fc, 4, false,0f,   300f,   -1000f,300f,   0.0f, 0f,"x / 1000",          "%05.1f","PUT Actual",          "kpa",   true, ""),
        PIDStruct(0xd0011eee, 2, false,0f,   300f,   -1000f,1000f,  0.0f, 0f,"x / 120.6017666543","%05.1f","PUT Setpoint",        "kpa",   true, ""),
        PIDStruct(0xd0013a44, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Actual",          "°",     true, ""),
        PIDStruct(0xd0013a42, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Limit",           "°",     true, ""),
        PIDStruct(0xd000f377, 1, false,0.5f, 1.3f,   -1000f,1000f,  0.0f, 0f,"x / 2.142128661087","%07.2f","Throttle Position",   "%",     true, ""),
        PIDStruct(0xd0015344, 2, true, -40f, 500f,   -1000f,1000f,  0.0f, 0f,"x / 32",            "%07.2f","Torque Actual",       "Nm",    true, ""),
        PIDStruct(0xd0011f0c, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Torque Limitation",   "",      true, ""),
        PIDStruct(0xd0012048, 2, true, -40f, 500f,   -1000f,500f,   0.0f, 0f,"x / 32",            "%03.2f","Torque Requested",    "Nm",    true, ""),
        PIDStruct(0xd000f3c1, 1, false,-100f,100f,   -1000f,120f,   0.9f, 0f,"x - 40",            "%01.0f","Transmission Temp",   "°C",    true, ""),
        PIDStruct(0xd0011e76, 2, false,0f,   195000f,-100f, 190000f,0.0f, 0f,"x * 6.1035",        "%05.0f","Turbo Speed",         "rpm",   true, ""),
        PIDStruct(0xd0019b75, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Valve Lift Position", "",      true, ""),
        PIDStruct(0xd00155b6, 2, false,0f,   220f,   -1000f,220f,   0.0f, 0f,"x / 100",           "%03.0f","Vehicle Speed",       "km/h",  true, ""),
        PIDStruct(0xd0011e10, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999997",  "%05.3f","Wastegate Actual",    "%",     true, ""),
        PIDStruct(0xd0015c2c, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999997",  "%05.3f","Wastegate Setpoint",  "%",     true, ""),
        PIDStruct(0xd0015c5e, 2, false,0f,   2f,     -1000f,1000f,  0.0f, 0f,"x / 32",            "%03.1f","Wastegate Flow Req",  "kg/h",  true, ""),
        PIDStruct(0xd001b6cd, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Cruise Control",      "",      true, ""),
    )

    var data22: Array<DATAStruct?>? = arrayOf(
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false),
        DATAStruct(0.0f, 0.0f, false, 1.0f, false)
    )

    var data3E: Array<DATAStruct?>? = arrayOf(
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    DATAStruct(0.0f, 0.0f, false, 1.0f, false),
    )

    fun getData(mode: UDSLoggingMode = UDSLogger.getMode()): Array<DATAStruct?>? {
        return when(mode) {
            UDSLoggingMode.MODE_22  -> data22
            UDSLoggingMode.MODE_3E  -> data3E
        }
    }

    fun resetData() {
        try {
            getData()?.let { dataList ->
                for (i in 0 until dataList.count()) {
                    val data = dataList[i]
                    data?.let {
                        val pid = getList()!![i]
                        pid?.let {
                            data.max = pid.value
                            data.min = pid.value
                        }
                    }
                }
            }
        } catch (e: Exception)
        {
            DebugLog.e(TAG, "Unable to reset min/max list.", e)
        }
    }

    fun updateData() {
        try {
            getData()?.let { dataList ->
                for (i in 0 until dataList.count()) {
                    val data = dataList[i]
                    data?.let {
                        val pid = getList()!![i]
                        pid?.let {
                            //set min/max
                            if (pid.value > data.max)
                                data.max = pid.value

                            if (pid.value < data.min)
                                data.min = pid.value

                            //Check to see if we should be warning user
                            data.warn = (pid.value > pid.warnMax) or (pid.value < pid.warnMin)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to update data.", e)
        }
    }

    fun getList(mode: UDSLoggingMode = UDSLogger.getMode()): Array<PIDStruct?>? {
        return when(mode) {
            UDSLoggingMode.MODE_22  -> list22
            UDSLoggingMode.MODE_3E  -> list3E
        }
    }

    fun buildData(mode: UDSLoggingMode = UDSLogger.getMode()) {
        val list = getList(mode)
        list?.let {
            when (mode) {
                UDSLoggingMode.MODE_22 -> data22 = arrayOfNulls(list.count())
                UDSLoggingMode.MODE_3E -> data3E = arrayOfNulls(list.count())
            }
            val data = getData(mode)
            data?.let {
                for (i in 0 until data.count()) {
                    val pid = list[i]!!
                    val newData = DATAStruct(pid.value, pid.value, false, 1.0f, false)
                    //Check for low value PIDS
                    var progMax = pid.progMax
                    var progMin = pid.progMin

                    //if progress bar is flipped
                    if (pid.progMin > pid.progMax) {
                        progMax = pid.progMin
                        progMin = pid.progMax
                        newData.inverted = true
                    }

                    //build progress multiplier
                    newData.multiplier = 100.0f / (progMax - progMin)

                    data[i] = newData
                }
            }
        }
    }

    fun setList(mode: UDSLoggingMode = UDSLogger.getMode(), list: Array<PIDStruct?>?) {
        list?.let {
            when (mode) {
                UDSLoggingMode.MODE_22 -> list22 = list
                UDSLoggingMode.MODE_3E -> list3E = list
            }
            buildData(mode)
        }
    }

    fun getPID(address: Long, mode: UDSLoggingMode = UDSLogger.getMode()): PIDStruct? {
        getList(mode)?.let { list ->
            for (i in 0 until list.count()) {
                list[i]?.let { pid ->
                    if(pid.address == address) {
                        return pid
                    }
                }
            }
        }

        return null
    }

    fun setValue(pid: PIDStruct?, x: Float): Float {
        pid?.let {
            //Used in smoothing calculation
            val previousValue = pid.value

            //eval expression
            try {
                pid.value = eval(pid.equation.replace("x", x.toString(), true))
            } catch (e: Exception) {
                pid.value = 0f
            }

            //Add smoothing
            if (pid.smoothing > 0f && pid.smoothing < 0.9751f)
                pid.value = ((1f - pid.smoothing) * pid.value) + (pid.smoothing * previousValue)

            return pid.value
        }

        return 0f
    }

    fun getValue(pid: PIDStruct?): Float {
        return pid?.value ?: 0f
    }
}