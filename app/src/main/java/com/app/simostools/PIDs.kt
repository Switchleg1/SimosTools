package com.app.simostools

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
                     var tabs: String,
                     var assignTo: String)

data class DATAStruct(var min: Float,
                      var max: Float,
                      var warn: Boolean,
                      var multiplier: Float,
                      var inverted: Boolean)

object PIDs {
    private val TAG         = "PIDs"
    private var mInited     = false
    private var list22d: Array<PIDStruct?>? = arrayOf(
        PIDStruct(0x2032, 2, false,0f,  2000f,  -1000f, 5000f,  0.0f, 0f,"x",                     "%01.0f","Airflow",             "kg/hr",  true, "Airflow", "f"),
        PIDStruct(0xffff, 1, false,0f,  2000f,  -1000f, 5000f,  0.0f, 0f,"f / r * 8333.33333333", "%01.0f","Airmass",             "mg/stk", true, "Airflow", ""),
        PIDStruct(0x13ca, 2, false,50f, 120f,   70f,    120.0f, 0.9f, 0f,"x / 120.60176665439",   "%01.1f","Ambient Press",       "kpa",    true, "Misc",    "a"),
        PIDStruct(0x1004, 2, true, -40f,50f,    -30f,   50f,    0.7f, 0f,"x / 128",               "%01.1f","Ambient Temp",        "°C",     true, "Misc",    ""),
        PIDStruct(0x14a6, 1, false,0f,  2f,     8f,     16f,    0.8f, 0f,"x * 0.1015625",         "%01.1f","Battery Voltage",     "V",      true, "Misc",    ""),
        PIDStruct(0xffff, 1, false,-15f,35f,    -15f,   35f,    0.0f, 0f,"(m - a) * 0.1450777202","%01.1f","Boost",               "psi",    true, "Airflow", ""),
        PIDStruct(0xFFFF, 1, false,0f,  300f,   -1000f, 1000f,  0.0f, 0f,"h",                     "%01.0f","Calc HP",             "hp",     true, "Power",   ""),
        PIDStruct(0xFFFF, 1, false,0f,  500f,   -1000f, 1000f,  0.0f, 0f,"t",                     "%01.0f","Calc TQ",             "nm",     true, "Power",   ""),
        PIDStruct(0x101e, 1, false,0f,  100f,   -1f,    110f,   0.0f, 0f,"x * 0.390625",          "%01.0f","Cooling Fan",         "%",      true, "Misc",    ""),
        PIDStruct(0x11cd, 1, false,-50f,120f,   -100f,  130f,   0.0f, 0f,"x - 40",                "%01.1f","Coolant Temp",        "°C",     true, "Misc",    ""),
        PIDStruct(0x14ec, 2, false,0f,  100f,   -1f,    90f,    0.0f, 0f,"x * 0.09765625",        "%01.1f","CPU Load",            "%",      true, "Misc",    ""),
        PIDStruct(0xf40C, 2, false,0f,  7000f,  -1f,    6000f,  0.0f, 0f,"x / 4",                 "%01.0f","Engine Speed",        "rpm",    true, "",        "r"),
        PIDStruct(0xf452, 2, false,0f,  100f,   -50f,   100f,   0.0f, 0f,"x / 2.55",              "%01.1f","Eth Content",         "%",      true, "",        ""),
        PIDStruct(0x201a, 2, true, -10f,10f,    -50f,   50f,    0.0f, 0f,"(x / 10) - 24",         "%01.1f","Exhaust Cam Pos",     "°",      true, "",        ""),
        PIDStruct(0x2027, 2, false,0f,  250f,   -1f,    300f,   0.0f, 0f,"x / 10",                "%01.0f","FP DI",               "bar",    true, "",        ""),
        PIDStruct(0x293b, 2, false,0f,  250f,   -1f,    300f,   0.0f, 0f,"x / 10",                "%01.0f","FP DI SP",            "bar",    true, "",        ""),
        PIDStruct(0x2025, 2, false,0f,  15f,    -1f,    15f,    0.5f, 0f,"x / 1000",              "%01.2f","FP MPI",              "bar",    true, "Fuel",    ""),
        PIDStruct(0x2932, 2, false,0f,  15f,    -1f,    15f,    0.5f, 0f,"x / 1000",              "%01.2f","FP MPI SP",           "bar",    true, "Fuel",    ""),
        PIDStruct(0x209a, 2, false,0f,  100f,   -1000f, 100f,   0.0f, 0f,"x / 100",               "%01.1f","HPFP Eff Vol",        "%",      true, "",        ""),
        PIDStruct(0xFFFF, 1, false,-35f,35f,    -1000f, 1000f,  0.0f, 0f,"s + l",                 "%01.0f","Fuel Trim",           "%",      true, "Fuel",    ""),
        PIDStruct(0x210f, 2, false,0f,  6f,     -1000f, 10f,    0.0f, 0f,"x",                     "%01.0f","Gear",                "",       true, "",        ""),
        PIDStruct(0x1001, 1, false,-40f,55f,    -35f,   50f,    0.7f, 0f,"x * 0.75 - 48",         "%01.1f","IAT",                 "°C",     true, "Misc",    ""),
        PIDStruct(0x2004, 2, true, -10f,10f,    -15f,   100f,   0.0f, 0f,"x / 100",               "%01.2f","Ign Avg",             "°",      true, "Ignition",""),
        PIDStruct(0x13a0, 2, false,0f,  190000f,-1000f, 10000f, 0.0f, 0f,"x / 250",               "%01.2f","Inj PW DI",           "ms",     true, "Ignition",""),
        PIDStruct(0x13ac, 2, false,0f,  190000f,-1000f, 10000f, 0.0f, 0f,"x / 250",               "%01.2f","Inj PW MPI",          "ms",     true, "Ignition",""),
        PIDStruct(0x201e, 2, true, -10f,10f,    -50f,   50f,    0.0f, 0f,"30 - (x / 10)",         "%01.1f","Int Cam Pos",         "°",      true, "",        ""),
        PIDStruct(0x200a, 2, true, 0f,  -5f,    -4f,    1000f,  0.7f, 0f,"x / 100",               "%01.2f","Knock Cyl 1",         "°",      true, "Ignition","w"),
        PIDStruct(0x200b, 2, true, 0f,  -5f,    -4f,    1000f,  0.7f, 0f,"x / 100",               "%01.2f","Knock Cyl 2",         "°",      true, "Ignition","x"),
        PIDStruct(0x200c, 2, true, 0f,  -5f,    -4f,    1000f,  0.7f, 0f,"x / 100",               "%01.2f","Knock Cyl 3",         "°",      true, "Ignition","y"),
        PIDStruct(0x200d, 2, true, 0f,  -5f,    -4f,    1000f,  0.7f, 0f,"x / 100",               "%01.2f","Knock Cyl 4",         "°",      true, "Ignition","z"),
        PIDStruct(0x10c0, 2, false,0f,  2f,     0.7f,   1000f,  0.0f, 0f,"x / 1024",              "%01.2f","Lambda",              "l",      true, "Fuel",    ""),
        PIDStruct(0xf444, 2, false,0f,  2f,     -1000f, 1000f,  0.0f, 0f,"x / 32768",             "%01.2f","Lambda SP",           "l",      true, "Fuel",    ""),
        PIDStruct(0x2028, 2, false,0f,  100f,   -1.0f,  100f,   0.7f, 0f,"x / 100",               "%01.1f","LPFP Duty",           "%",      true, "Fuel",    ""),
        PIDStruct(0xf456, 1, false,-25f,25f,    -20f,   20f,    0.7f, 0f,"x / 1.28 - 100",        "%01.1f","LTFT",                "%",      true, "Fuel",    "l"),
        PIDStruct(0x39c0, 2, false,0f,  300f,   -1000f, 300f,   0.0f, 0f,"x / 10",                "%01.0f","MAP",                 "kpa",    true, "Airflow", "m"),
        PIDStruct(0x39c1, 2, false,0f,  300f,   -1000f, 300f,   0.0f, 0f,"x / 10",                "%01.0f","MAP SP",              "kpa",    true, "Airflow", ""),
        PIDStruct(0x2904, 2, false,0f,  20f,    -1000f, 10f,    0.0f, 0f,"x",                     "%01.0f","Misfires",            "",       true, "Misc",    ""),
        PIDStruct(0x202f, 2, false,-50f,130f,   0f,     120f,   0.9f, 0f,"(x - 2731.4) / 10",     "%01.1f","Oil Temp",            "°C",     true, "Misc",    ""),
        PIDStruct(0x1070, 2, false,0f,  100f,   -1000f, 1000f,  0.0f, 0f,"x / 10.24",             "%01.0f","Pedal Pos",           "kpa",    true, "Airflow", ""),
        PIDStruct(0x295c, 1, false,0f,  1f,     -1000f, 1000f,  0.0f, 0f,"x",                     "%01.1f","Port Flap Pos",       "",       true, "",        ""),
        PIDStruct(0x202a, 2, false,0f,  300f,   -1000f, 300f,   0.0f, 0f,"x / 10",                "%01.1f","PUT",                 "kpa",    true, "Airflow", "p"),
        PIDStruct(0x2029, 2, false,0f,  300f,   -1000f, 300f,   0.0f, 0f,"x / 10",                "%01.0f","PUT SP",              "kpa",    true, "Airflow", ""),
        PIDStruct(0xf406, 1, false,-25f,25f,    -20f,   20f,    0.0f, 0f,"x / 1.28 - 100",        "%01.1f","STFT",                "%",      true, "Fuel",    "s"),
        PIDStruct(0x437C, 2, true, -50f,450f,   -1000f, 500f,   0.0f, 0f,"x / 10",                "%01.2f","Torque",              "Nm",     true, "Power",   ""),
        PIDStruct(0x4380, 2, true, 0f,  500f,   -1000f, 500f,   0.7f, 0f,"x / 10",                "%01.1f","Torque Req",          "Nm",     true, "Power",   ""),
        PIDStruct(0x20ba, 2, true, 0f,  100f,   -1000f, 101f,   0.0f, 0f,"x / 10",                "%01.1f","TPS",                 "%",      true, "",        ""),
        PIDStruct(0x1040, 2, false,0f,  190f,   -1000f, 195f,   0.0f, 0f,"x / 163.84",            "%01.0f","Turbo Speed",         "rpm",    true, "Airflow", ""),
        PIDStruct(0x1041, 2, true, -40f,55f,    -500f,  500f,   0.7f, 0f,"x * 0.005859375 + 144", "%01.1f","Turbo Air Temp",      "°C",     true, "Airflow", ""),
        PIDStruct(0x167c, 1, false,0f,  1f,     -100f,  100f,   0.0f, 0f,"x",                     "%01.0f","Valve Lift Pos",      "",       true, "",        ""),
        PIDStruct(0x2033, 2, false,0f,  220f,   -20f,   220f,   0.0f, 0f,"x / 347.947",           "%01.1f","Vehicle Speed",       "km/hr",  true, "Airflow", ""),
        PIDStruct(0x39a2, 2, false,0f,  100f,   -1000f, 1000f,  0.0f, 0f,"100 - x / 100",         "%01.1f","Wastegate",           "%",      true, "",        ""),
        PIDStruct(0x39a3, 2, false,0f,  100f,   -1000f, 1000f,  0.0f, 0f,"100 - x / 100",         "%01.1f","Wastegate SP",        "%",      true, "",        ""),
        PIDStruct(0x203c, 2, false,0f,  1f,     -1f,    100f,   0.0f, 0f,"x",                     "%01.0f","Cruise",              "",       true, "",        ""),
    )

    private var list3Ed: Array<PIDStruct?>? = arrayOf(
        PIDStruct(0xd000ee2a, 1, false,-10f, 10f,    -1000f,1000f,  0.8f, 0f,"(x - 127) / 10",    "%01.2f","Accel. Lat",          "m/s2",   true, "",        ""),
        PIDStruct(0xd00141ba, 2, false,-10f, 10f,    -1000f,1000f,  0.4f, 0f,"(x - 512) / 32",    "%01.2f","Accel. Long",         "m/s2",   true, "",        ""),
        PIDStruct(0xd00097b4, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%01.2f","Airmass",             "g/stk",  true, "Airflow", ""),
        PIDStruct(0xd00097fc, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%01.2f","Airmass SP",          "g/stk",  true, "Airflow", ""),
        PIDStruct(0xd0013c76, 2, false,0f,   110f,   0f,    120f,   0.9f, 0f,"x / 120.60176665",  "%01.2f","Ambient Press",       "kpa",    true, "Misc",    "a"),
        PIDStruct(0xd000c177, 1, false,-25f, 45f,    -100f, 100f,   0.9f, 0f,"(x - 64) / 1.33",   "%01.2f","Ambient Temp",        "°C",     true, "Misc",    ""),
        PIDStruct(0xd0015172, 2, true, 10f,  15f,    7f,    16f,    0.7f, 0f,"x / 51.2",          "%01.1f","Battery Volts",       "V",      true, "Misc",    ""),
        PIDStruct(0xFFFFFFFF, 1, false,-15f, 35f,    -15f,  35f,    0.0f, 0f,"(m - a) * 0.145077","%01.1f","Boost",               "psi",    true, "Airflow", ""),
        PIDStruct(0xFFFFFFFF, 1, false,0f,   500f,   -1000f,1000f,  0.0f, 0f,"t",                 "%01.0f","Calc TQ",             "nm",     true, "Power",   ""),
        PIDStruct(0xFFFFFFFF, 1, false,0f,   300f,   -1000f,1000f,  0.0f, 0f,"h",                 "%01.0f","Calc HP",             "hp",     true, "Power",   ""),
        PIDStruct(0xd000c36e, 1, false,-10f, 10f,    -100f, 100f,   0.0f, 0f,"x",                 "%01.2f","Comb Mode",           "",       true, "",        ""),
        PIDStruct(0xd000c6f5, 1, false,-50f, 130f,   -100f, 150f,   0.8f, 0f,"(x - 64) / 1.33",   "%01.0f","Coolant Temp",        "°C",     true, "Misc",    ""),
        PIDStruct(0xd0013982, 2, false,0f,   720f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Actual",          "°",      true, "",        ""),
        PIDStruct(0xd001397a, 2, false,0f,   720f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Limit",           "°",      true, "",        ""),
        PIDStruct(0xd0012400, 2, false,0f,   7000f,  -1.0f, 6000f,  0.0f, 0f,"x",                 "%01.0f","Engine Speed",        "rpm",    true, "",        ""),
        PIDStruct(0xd000c1d4, 1, false,0f,   100f,   -100f, 100f,   0.0f, 0f,"x / 2.55",          "%01.0f","Eth Content",         "%",      true, "",        ""),
        PIDStruct(0xd001566e, 2, true, -45f, 45f,    -100f, 100f,   0.0f, 0f,"x / 128",           "%01.1f","Exh Cam Position",    "°",      true, "",        ""),
        PIDStruct(0xd0011e04, 2, false,0f,   1.5f,   -1f,   10f,    0.0f, 0f,"x / 16384",         "%01.2f","Exh Flow Factor",     "",       true, "",        ""),
        PIDStruct(0xd0011eba, 2, false,0f,   500f,   -100f, 1000f,  0.0f, 0f,"x / 120.60176665",  "%01.0f","Exh Pres Desired",    "kpa",    true, "",        ""),
        PIDStruct(0xd00136ac, 2, false,0f,   28000f, 0f,    28000f, 0.0f, 0f,"x / 1.884402603",   "%01.0f","FP DI",               "kpa",    true, "Fuel",    ""),
        PIDStruct(0xd0013640, 2, false,0f,   28000f, -1000f,30000f, 0.0f, 0f,"x / 1.8844026039",  "%01.0f","FP DI SP",            "kpa",    true, "Fuel",    ""),
        PIDStruct(0xd0011b26, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%01.0f","FP MPI",              "kpa",    true, "Fuel",    ""),
        PIDStruct(0xd001360c, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%01.0f","FP MPI SP",           "kpa",    true, "Fuel",    ""),
        PIDStruct(0xd0013636, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow",           "mg/stk", true, "Fuel",    ""),
        PIDStruct(0xd00135e0, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow SP",        "mg/stk", true, "Fuel",    ""),
        PIDStruct(0xd00192b1, 1, false,-100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Fuel Split MPI",      "",       true, "Fuel",    ""),
        PIDStruct(0xFFFFFFFF, 1, false,-35f, 35f,    -1000f,1000f,  0.0f, 0f,"s + l",             "%01.1f","Fuel Trim",           "%",      true, "Fuel",    ""),
        PIDStruct(0xd000f39a, 1, false,0f,   6f,     -1f,   7f,     0.0f, 0f,"x",                 "%01.0f","Gear",                "gear",   true, "",        ""),
        PIDStruct(0xd001363c, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999999",  "%01.0f","HPFP Eff Vol",        "%",      true, "",        ""),
        PIDStruct(0xd000c179, 1, false,-50f, 70f,    -20f,  50f,    0.9f, 0f,"(x - 64) / 1.33",   "%01.1f","IAT",                 "°C",     true, "Misc",    ""),
        PIDStruct(0xd000e57e, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95) / 2.66667","%01.0f","Ign Table",           "°",      true, "Ignition",""),
        PIDStruct(0xd000e59c, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95) / 2.66667","%01.0f","Ign Avg",             "°",      true, "Ignition",""),
        PIDStruct(0xd0013b16, 2, false,-25f, 25f,    -1000f,1000f,  0.0f, 0f,"x / 250",           "%01.1f","Inj PW DI",           "ms",     true, "Fuel",    ""),
        PIDStruct(0xd0013824, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 250",           "%01.1f","Inj PW MPI",          "ms",     true, "Fuel",    ""),
        PIDStruct(0xd001566c, 2, true, -100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Intake Cam Pos",      "°",      true, "",        ""),
        PIDStruct(0xd0011e08, 2, false,0f,   20f,    -1000f,1000f,  0.0f, 0f,"x / 16384",         "%01.0f","Intake Flow Fact",    "",       true, "",        ""),
        PIDStruct(0xd001988e, 1, false,0f,   -5f,    -3.0f, 3f,     0.8f, 0f,"(x - 128) / 2.66",  "%01.2f","Knock Avg",           "°",      true, "Ignition",""),
        PIDStruct(0xd000efb1, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%01.2f","Knock Cyl 1",         "°",      true, "Ignition",""),
        PIDStruct(0xd000efb2, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%01.2f","Knock Cyl 2",         "°",      true, "Ignition",""),
        PIDStruct(0xd000efb3, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%01.2f","Knock Cyl 3",         "°",      true, "Ignition",""),
        PIDStruct(0xd000efb4, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%01.2f","Knock Cyl 4",         "°",      true, "Ignition",""),
        PIDStruct(0xd00120e2, 2, false,0.5f, 1.5f,   -0.1f, 5f,     0.0f, 0f,"x / 32767.999999",  "%01.2f","Lambda",              "l",      true, "Fuel",    ""),
        PIDStruct(0xd00143f6, 2, false,0f,   2f,     -100f, 500f,   0.0f, 0f,"x / 1024",          "%01.2f","Lambda SP",           "l",      true, "Fuel",    ""),
        PIDStruct(0xd0013600, 2, false,0f,   100f,   -1000f,100f,   0.0f, 0f,"x / 655.35999999",  "%01.0f","LPFP Duty",           "%",      true, "Fuel",    ""),
        PIDStruct(0xd000f00b, 1, false,-25f, 25f,    -20f,  20f,    0.7f, 0f,"x / 1.28 - 100.0",  "%01.1f","LTFT",                "%",      true, "Fuel",    "l"),
        PIDStruct(0xd00098cc, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000",          "%01.1f","MAP",                 "kpa",    true, "Airflow", "m"),
        PIDStruct(0xd00098f4, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000",          "%01.1f","MAP SP",              "kpa",    true, "Airflow", ""),
        PIDStruct(0xd000e578, 1, true, 0f,   220f,   -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.6667","%01.2f","OF Ign Cor",          "°CRK",   true, "",        ""),
        PIDStruct(0xd001de8d, 1, true, 0f,   7000f,  -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.6667","%01.2f","OF Ign Max",          "°CRK",   true, "",        ""),
        PIDStruct(0xd001de8e, 1, false,0f,   3f,     -1000f,1000f,  0.0f, 0f,"x / 128",           "%01.2f","OF Ign Fac",          "",       true, "",        ""),
        PIDStruct(0xd001de8e, 1, false,0f,   3f,     -1000f,1000f,  0.0f, 0f,"x / 128",           "%01.2f","OF Ign Fac",          "",       true, "",        ""),
        PIDStruct(0xd0017f52, 2, false,0f,   2f,     -1000f,1000f,  0.0f, 0f,"x / 32768",         "%01.2f","OF Tq Mult",          "%",      true, "",        ""),
        PIDStruct(0xd000c5ae, 1, false,-25f, 120f,   -1000f,1000f,  0.9f, 0f,"x - 40",            "%01.0f","Oil Temp",            "°C",     true, "Misc",    ""),
        PIDStruct(0xd0012028, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 10.24",         "%01.1f","Pedal Pos",           "",       true, "",        ""),
        PIDStruct(0xd0000aa1, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Port Flap Pos",       "%",      true, "",        ""),
        PIDStruct(0xd00098fc, 4, false,0f,   300f,   -1000f,300f,   0.0f, 0f,"x / 1000",          "%01.1f","PUT",                 "kpa",    true, "Airflow", "p"),
        PIDStruct(0xd0011eee, 2, false,0f,   300f,   -1000f,1000f,  0.0f, 0f,"x / 120.601766",    "%01.1f","PUT SP",              "kpa",    true, "Airflow", ""),
        PIDStruct(0xd0013a44, 2, false,0f,   720f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Actual",          "°",      true, "",        ""),
        PIDStruct(0xd0013a42, 2, false,0f,   720f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Limit",           "°",      true, "",        ""),
        PIDStruct(0xd000f00c, 1, false,-25f, 25f,    -20f,  20f,    0.8f, 0f,"x / 1.28 - 100",    "%01.0f","STFT",                "%",      true, "Fuel",    "s"),
        PIDStruct(0xd0015344, 2, true, -40f, 500f,   -1000f,1000f,  0.0f, 0f,"x / 32",            "%01.1f","Torque",              "Nm",     true, "Power",   ""),
        PIDStruct(0xd0011f0c, 2, false,0f,   512f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Torque Lim",          "",       true, "Power",   ""),
        PIDStruct(0xd0012048, 2, true, -40f, 500f,   -1000f,500f,   0.0f, 0f,"x / 32",            "%01.1f","Torque Req",          "Nm",     true, "Power",   ""),
        PIDStruct(0xd000f377, 1, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 2.142128661087","%01.2f","TPS",                 "%",      true, "",        ""),
        PIDStruct(0xd000f3c1, 1, false,-100f,100f,   -1000f,120f,   0.9f, 0f,"x - 40",            "%01.0f","Trans Temp",          "°C",     true, "Misc",    ""),
        PIDStruct(0xd0011e76, 2, false,0f,   195f,   -100f, 190f,   0.0f, 0f,"x / 163.84",        "%01.1f","Turbo Speed",         "rpm",    true, "Airflow", ""),
        PIDStruct(0xd0019b75, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Valve Lift Pos",      "",       true, "",        ""),
        PIDStruct(0xd00155b6, 2, false,0f,   220f,   -1000f,220f,   0.0f, 0f,"x / 100",           "%01.0f","Vehicle Speed",       "km/h",   true, "",        ""),
        PIDStruct(0xd0011e10, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999997",  "%01.1f","Wastegate",           "%",      true, "Airflow", ""),
        PIDStruct(0xd0015c2c, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999997",  "%01.1f","Wastegate SP",        "%",      true, "Airflow", ""),
        PIDStruct(0xd0015c5e, 2, false,0f,   2f,     -1000f,1000f,  0.0f, 0f,"x / 32",            "%01.1f","Wastegate Flow",      "kg/h",   true, "",        ""),
        PIDStruct(0xd001b6cd, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Cruise",              "",       true, "",        ""),
    )
    var list22: Array<PIDStruct?>? = null
    var list3E: Array<PIDStruct?>? = null
    var data22: Array<DATAStruct?>? = null
    var data3E: Array<DATAStruct?>? = null
    var assign22 = mutableMapOf<String, Int>()
    var assign3E = mutableMapOf<String, Int>()
    var tabs22 = mutableMapOf<String, Boolean>()
    var tabs3E = mutableMapOf<String, Boolean>()

    fun init() {
        if(!mInited) {
            loadDefaultPIDS(UDSLoggingMode.MODE_22)
            loadDefaultPIDS(UDSLoggingMode.MODE_3E)

            mInited = true
        }
    }

    fun clear() {
        list22 = null
        list3E = null
        data22 = null
        data3E = null
        assign22.clear()
        assign3E.clear()
        tabs22.clear()
        tabs3E.clear()

        mInited = false
    }

    fun loadDefaultPIDS(mode: UDSLoggingMode = UDSLogger.getMode()) {
        when (mode) {
            UDSLoggingMode.MODE_22 -> list22 = list22d?.clone()
            UDSLoggingMode.MODE_3E -> list3E = list3Ed?.clone()
        }
        buildAssign(mode)
        buildData(mode)
        buildTabs(mode)
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
            DebugLog.d(TAG, "Reset min/max list.")
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

    fun getTabs(mode: UDSLoggingMode = UDSLogger.getMode()): MutableMap<String, Boolean> {
        return when(mode) {
            UDSLoggingMode.MODE_22  -> tabs22
            UDSLoggingMode.MODE_3E  -> tabs3E
        }
    }

    fun getData(mode: UDSLoggingMode = UDSLogger.getMode()): Array<DATAStruct?>? {
        return when(mode) {
            UDSLoggingMode.MODE_22  -> data22
            UDSLoggingMode.MODE_3E  -> data3E
        }
    }

    fun getAssign(mode: UDSLoggingMode = UDSLogger.getMode()): MutableMap<String, Int> {
        return when(mode) {
            UDSLoggingMode.MODE_22  -> assign22
            UDSLoggingMode.MODE_3E  -> assign3E
        }
    }

    fun buildTabs(mode: UDSLoggingMode = UDSLogger.getMode()) {
        getList(mode)?.let { list ->
            when (mode) {
                UDSLoggingMode.MODE_22 -> tabs22 = mutableMapOf()
                UDSLoggingMode.MODE_3E -> tabs3E = mutableMapOf()
            }
            val tabs = getTabs(mode)
            for (i in 0 until list.count()) {
                list[i]?.let { pid ->
                    pid.tabs.split(".").forEach() {
                        val tabName = it.substringBefore("|")
                        tabs[tabName] = true
                        DebugLog.d(TAG, "buildTabs: Added $tabName")
                    }
                }
            }
        }
    }

    fun buildAssign(mode: UDSLoggingMode = UDSLogger.getMode()) {
        getList(mode)?.let { list ->
            when (mode) {
                UDSLoggingMode.MODE_22 -> assign22 = mutableMapOf()
                UDSLoggingMode.MODE_3E -> assign3E = mutableMapOf()
            }
            val assign = getAssign(mode)
            for (i in 0 until list.count()) {
                list[i]?.let { pid ->
                    if (pid.assignTo.length == 1 && pid.assignTo[0] >= 'a' && pid.assignTo[0] <= 'z' && pid.assignTo[0] != 'x' && pid.assignTo[0] != 'h' && pid.assignTo[0] != 't') {
                        assign[pid.assignTo] = i
                        DebugLog.d(TAG, "buildAssign: Added ${pid.name} as ${pid.assignTo}")
                    }
                }
            }
        }
    }

    fun buildData(mode: UDSLoggingMode = UDSLogger.getMode()) {
        getList(mode)?.let { list->
            when (mode) {
                UDSLoggingMode.MODE_22 -> data22 = arrayOfNulls(list.count())
                UDSLoggingMode.MODE_3E -> data3E = arrayOfNulls(list.count())
            }
            val data = getData(mode)
            data?.let {
                for (i in 0 until data.count()) {
                    list[i]?.let { pid ->
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
    }

    fun setList(mode: UDSLoggingMode = UDSLogger.getMode(), list: Array<PIDStruct?>?) {
        list?.let {
            when (mode) {
                UDSLoggingMode.MODE_22 -> list22 = list
                UDSLoggingMode.MODE_3E -> list3E = list
            }
            buildAssign(mode)
            buildData(mode)
            buildTabs(mode)
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
                var equationString = pid.equation.replace("x", x.toString(), true)
                equationString = equationString.replace("t", UDSLogger.getTQ().toString(), true)
                equationString = equationString.replace("h", UDSLogger.getHP().toString(), true)
                getList()?.let { list ->
                    getAssign().forEach {
                        equationString =
                            equationString.replace(it.key, list[it.value]!!.value.toString(), true)
                    }
                }
                pid.value = eval(equationString)
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