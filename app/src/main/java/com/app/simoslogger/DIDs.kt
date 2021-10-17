package com.app.simoslogger

import java.lang.Exception

data class DIDStruct(var address: Long,
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
                     var unit: String)

object DIDs {
    private val TAG = "DIDs"

    var list22a: Array<DIDStruct?>? = arrayOf(
        DIDStruct(0x2033, 2, false,0f,  220f,   -20f, 200f,   0.0f, 0f,"x / 347.947",           "%05.1f","Vehicle Speed",       "km/hr"),
        DIDStruct(0xf40C, 2, false,0f,  7000f,  -1f,  6000f,  0.0f, 0f,"x / 4",                 "%05.1f","Engine Speed",        "rpm"),
        DIDStruct(0x39c0, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Actual",          "kpa"),
        DIDStruct(0x39c1, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Setpoint",        "kpa"),
        DIDStruct(0x39c2, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","PUT Actual",          "kpa"),
        DIDStruct(0x10c0, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Actual",       "l"),
        DIDStruct(0x2950, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Setpoint",     "l"),
        DIDStruct(0x39a2, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Actual",    "%"),
        DIDStruct(0x39a3, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Setpoint",  "%"),
        DIDStruct(0x2032, 2, false,0f,  1500f,  -1f,  1500f,  0.0f, 0f,"x / 2.4",               "%04.2f","Airmass Actual",      "g/s"),
        DIDStruct(0x13a0, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 DI","ms"),
        DIDStruct(0x13a4, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 PI","ms"),
        DIDStruct(0x13a8, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 MP","ms"),
        DIDStruct(0x437C, 2, true, -50f,450f,   -100f,500f,   0.0f, 0f,"x / 10",                "%03.2f","Torque Actual",       "Nm"),
        DIDStruct(0x4380, 2, true, 0f,  500f,   -100f,500f,   0.7f, 0f,"x / 10",                "%04.1f","Torque Requested",    "Nm"),
        DIDStruct(0x2027, 2, false,0f,  28000f, -1f,  28000f, 0.0f, 0f,"x * 10",                "%04.1f","HPFP Actual",         "kpa"),
        DIDStruct(0x293b, 2, false,0f,  25000f, 600f, 25000f, 0.0f, 0f,"x * 10",                "%05.0f","HPFP Setpoint",       "kpa"),
        DIDStruct(0x209a, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"x / 100",               "%04.1f","HPFP Volume",         "%"),
        DIDStruct(0x2025, 2, false,0f,  1500f,  600f, 1500f,  0.7f, 0f,"x / 10",                "%03.0f","LPFP Actual",         "kpa"),
        DIDStruct(0x2028, 2, false,0f,  100f,   -1.0f,100f,   0.7f, 0f,"x / 100",               "%04.1f","LPFP Duty",           "%"),
        DIDStruct(0x293b, 2, false,0f,  25000f, 600f, 25000f, 0.0f, 0f,"x / 10",                "%05.0f","LPFP Setpoint",       "kpa"),
        DIDStruct(0xf456, 1, false,-25f,25f,    -20f, 20f,    0.7f, 0f,"x / 1.28 - 100",        "%04.1f","Fuel Trim Long Term", "%"),
        DIDStruct(0xf406, 1, false,-25f,25f,    -20f, 20f,    0.0f, 0f,"x / 1.28 - 100",        "%04.1f","Fuel Trim Short Term","%"),
        DIDStruct(0x20ba, 2, true, 0f,  100f,   -1f,  101f,   0.0f, 0f,"x / 10",                "%04.1f","Throttle Sensor",     "%"),
        DIDStruct(0x1040, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x * 6.1035",            "%06.0f","Turbo Speed",         "rpm"),
        DIDStruct(0x2004, 2, true, -10f,10f,    -15f, 60f,    0.0f, 0f,"x / 100",               "%04.2f","Ignition Actual",     "°"),
        DIDStruct(0x200a, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 1",   "°"),
        DIDStruct(0x200b, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 2",   "°"),
        DIDStruct(0x200c, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 3",   "°"),
        DIDStruct(0x200d, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 4",   "°"),
        DIDStruct(0x1001, 1, false,-40f,55f,    -35f, 50f,    0.7f, 0f,"x * 0.75 - 48",         "%03.1f","Intake Air Temp",     "°C"),
        DIDStruct(0x1041, 1, true, -40f,55f,    -35f, 50f,    0.7f, 0f,"x * 0.005859375 + 144", "%03.1f","Turbo Air Temp",      "°C"),
        DIDStruct(0x295c, 1, false,0f,  1f,     -1f,  2f,     0.0f, 0f,"x",                     "%01.0f","Flaps Actual",        ""),
        DIDStruct(0x295d, 1, false,0f,  1f,     -1f,  2f,     0.0f, 0f,"x",                     "%01.0f","Flaps Setpoint",      ""),
        DIDStruct(0x2904, 2, false,0f,  20f,    -1f,  10f,    0.0f, 0f,"x",                     "%02.0f","Misfire Sum Global",  ""),
        DIDStruct(0x202f, 2, false,-50f,130f,   0f,   120f,   0.9f, 0f,"(x - 2731.4) / 10",     "%04.1f","Oil Temp",            "°C"),
        DIDStruct(0x13ca, 2, false,50f, 120f,   70f,  120.0f, 0.9f, 0f,"x / 120.60176665439",   "%03.0f","Ambient Pressure",    "kpa"),
        DIDStruct(0x1004, 2, true, -40f,50f,    -30f, 45f,    0.7f, 0f,"x / 128",               "%05.2f","Ambient Air Temp",    "°C"),
        DIDStruct(0x101e, 1, false,0f,  100f,   -1f,  110f,   0.0f, 0f,"x * 0.390625",          "%03.1f","Cooling Fan",         "%"),
        DIDStruct(0x14ec, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"x * 0.09765625",        "%03.1f","CPU Load",            "%"),
        DIDStruct(0x167c, 1, false,0f,  1f,     -1f,  100f,   0.0f, 0f,"x",                     "%03.1f","Valve Lift Position", ""),
        DIDStruct(0x201a, 2, true, -10f,10f,    -50f, 50f,    0.0f, 0f,"x / 10",                "%03.1f","Exhaust Cam Position","°"),
        DIDStruct(0x201e, 2, true, -10f,10f,    -50f, 50f,    0.0f, 0f,"x / 10",                "%03.1f","Intake Cam Position", "°"),
        DIDStruct(0x210f, 2, false,0f,  6f,     -1f,  10f,    0.0f, 0f,"x",                     "%01.0f","Gear",                ""),
        DIDStruct(0x11cd, 1, false,0f,  6f,     -1f,  10f,    0.0f, 0f,"x - 40",                "%02.0f","Coolant Temp",        "°C"),
        DIDStruct(0x14a6, 1, false,0f,  2f,     8f,   16f,    0.8f, 0f,"x * 0.1015625",         "%01.0f","Battery Voltage",     "V"),
        DIDStruct(0x203c, 2, false,0f,  2f,     -1f,  100f,   0.0f, 0f,"x",                     "%01.0f","Cruise Control",      ""),
    )

    var list22b: Array<DIDStruct?>? = arrayOf(
        DIDStruct(0x2033, 2, false,0f,  220f,   -20f, 200f,   0.0f, 0f,"x / 347.947",           "%05.1f","Vehicle Speed",       "km/hr"),
        DIDStruct(0xf40C, 2, false,0f,  7000f,  -1f,  6000f,  0.0f, 0f,"x / 4",                 "%05.1f","Engine Speed",        "rpm"),
        DIDStruct(0x39c0, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Actual",          "kpa"),
        DIDStruct(0x39c1, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Setpoint",        "kpa"),
        DIDStruct(0x39c2, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","PUT Actual",          "kpa"),
        DIDStruct(0x10c0, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Actual",       "l"),
        DIDStruct(0x2950, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Setpoint",     "l"),
        DIDStruct(0x39a2, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Actual",    "%"),
        DIDStruct(0x39a3, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Setpoint",  "%"),
        DIDStruct(0x2032, 2, false,0f,  1500f,  -1f,  1500f,  0.0f, 0f,"x / 2.4",               "%04.2f","Airmass Actual",      "g/s"),
        DIDStruct(0x13a0, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 DI","ms"),
        DIDStruct(0x437C, 2, true, -50f,450f,   -100f,500f,   0.0f, 0f,"x / 10",                "%03.2f","Torque Actual",       "Nm"),
        DIDStruct(0x4380, 2, true, 0f,  500f,   -100f,500f,   0.7f, 0f,"x / 10",                "%04.1f","Torque Requested",    "Nm"),
        DIDStruct(0x2027, 2, false,0f,  28000f, -1f,  28000f, 0.0f, 0f,"x * 10",                "%04.1f","HPFP Actual",         "kpa"),
        DIDStruct(0x293b, 2, false,0f,  25000f, 600f, 25000f, 0.0f, 0f,"x * 10",                "%05.0f","HPFP Setpoint",       "kpa"),
        DIDStruct(0x2025, 2, false,0f,  1500f,  600f, 1500f,  0.7f, 0f,"x / 10",                "%03.0f","LPFP Actual",         "kpa"),
        DIDStruct(0x2028, 2, false,0f,  100f,   -1.0f,100f,   0.7f, 0f,"x / 100",               "%04.1f","LPFP Duty",           "%"),
        DIDStruct(0x293b, 2, false,0f,  25000f, 600f, 25000f, 0.0f, 0f,"x / 10",                "%05.0f","LPFP Setpoint",       "kpa"),
        DIDStruct(0xf406, 1, false,-25f,25f,    -20f, 20f,    0.0f, 0f,"x / 1.28 - 100",        "%04.1f","Fuel Trim Short Term","%"),
        DIDStruct(0x20ba, 2, true, 0f,  100f,   -1f,  101f,   0.0f, 0f,"x / 10",                "%04.1f","Throttle Sensor",     "%"),
        DIDStruct(0x2004, 2, true, -10f,10f,    -15f, 60f,    0.0f, 0f,"x / 100",               "%04.2f","Ignition Actual",     "°"),
        DIDStruct(0x200a, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 1",   "°"),
        DIDStruct(0x200b, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 2",   "°"),
        DIDStruct(0x200c, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 3",   "°"),
        DIDStruct(0x200d, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 4",   "°"),
        DIDStruct(0x1001, 1, false,-40f,55f,    -35f, 50f,    0.7f, 0f,"x * 0.75 - 48",         "%03.1f","Intake Air Temp",     "°C"),
        DIDStruct(0x295c, 1, false,0f,  1f,     -1f,  2f,     0.0f, 0f,"x",                     "%01.0f","Flaps Actual",        ""),
        DIDStruct(0x167c, 1, false,0f,  1f,     -1f,  100f,   0.0f, 0f,"x",                     "%03.1f","Valve Lift Position", ""),
        DIDStruct(0x201a, 2, true, -10f,10f,    -50f, 50f,    0.0f, 0f,"x / 10",                "%03.1f","Exhaust Cam Position","°"),
        DIDStruct(0x201e, 2, true, -10f,10f,    -50f, 50f,    0.0f, 0f,"x / 10",                "%03.1f","Intake Cam Position", "°"),
        DIDStruct(0x210f, 2, false,0f,  6f,     -1f,  10f,    0.0f, 0f,"x",                     "%01.0f","Gear",                ""),
        DIDStruct(0x203c, 2, false,0f,  2f,     -1f,  100f,   0.0f, 0f,"x",                     "%01.0f","Cruise Control",      ""),
    )

    var list22c: Array<DIDStruct?>? = arrayOf(
        DIDStruct(0x2033, 2, false,0f,  220f,   -20f, 200f,   0.0f, 0f,"x / 347.947",           "%05.1f","Vehicle Speed",       "km/hr"),
        DIDStruct(0xf40C, 2, false,0f,  7000f,  -1f,  6000f,  0.0f, 0f,"x / 4",                 "%05.1f","Engine Speed",        "rpm"),
        DIDStruct(0x39c0, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Actual",          "kpa"),
        DIDStruct(0x39c1, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","MAP Setpoint",        "kpa"),
        DIDStruct(0x39c2, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10",                "%05.1f","PUT Actual",          "kpa"),
        DIDStruct(0x10c0, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Actual",       "l"),
        DIDStruct(0x2950, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%04.3f","Lambda Setpoint",     "l"),
        DIDStruct(0x39a2, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Actual",    "%"),
        DIDStruct(0x39a3, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100 - x / 100",         "%04.1f","Wastegate Setpoint",  "%"),
        DIDStruct(0x2032, 2, false,0f,  1500f,  -1f,  1500f,  0.0f, 0f,"x / 2.4",               "%04.2f","Airmass Actual",      "g/s"),
        DIDStruct(0x13a0, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%04.2f","Injector PW Cyl 1 DI","ms"),
        DIDStruct(0x437C, 2, true, -50f,450f,   -100f,500f,   0.0f, 0f,"x / 10",                "%03.2f","Torque Actual",       "Nm"),
        DIDStruct(0x2027, 2, false,0f,  28000f, -1f,  28000f, 0.0f, 0f,"x * 10",                "%04.1f","HPFP Actual",         "kpa"),
        DIDStruct(0x2025, 2, false,0f,  1500f,  600f, 1500f,  0.7f, 0f,"x / 10",                "%03.0f","LPFP Actual",         "kpa"),
        DIDStruct(0x2028, 2, false,0f,  100f,   -1.0f,100f,   0.7f, 0f,"x / 100",               "%04.1f","LPFP Duty",           "%"),
        DIDStruct(0xf406, 1, false,-25f,25f,    -20f, 20f,    0.0f, 0f,"x / 1.28 - 100",        "%04.1f","Fuel Trim Short Term","%"),
        DIDStruct(0x20ba, 2, true, 0f,  100f,   -1f,  101f,   0.0f, 0f,"x / 10",                "%04.1f","Throttle Sensor",     "%"),
        DIDStruct(0x2004, 2, true, -10f,10f,    -15f, 60f,    0.0f, 0f,"x / 100",               "%04.2f","Ignition Actual",     "°"),
        DIDStruct(0x200a, 2, true, 0f,  -5f,    -4f,  1f,     0.7f, 0f,"x / 100",               "%04.2f","Retard cylinder 1",   "°"),
        DIDStruct(0x1001, 1, false,-40f,55f,    -35f, 50f,    0.7f, 0f,"x * 0.75 - 48",         "%03.1f","Intake Air Temp",     "°C"),
        DIDStruct(0x295c, 1, false,0f,  1f,     -1f,  2f,     0.0f, 0f,"x",                     "%01.0f","Flaps Actual",        ""),
        DIDStruct(0x167c, 1, false,0f,  1f,     -1f,  100f,   0.0f, 0f,"x",                     "%03.1f","Valve Lift Position", ""),
        DIDStruct(0x210f, 2, false,0f,  6f,     -1f,  10f,    0.0f, 0f,"x",                     "%01.0f","Gear",                ""),
        DIDStruct(0x203c, 2, false,0f,  2f,     -1f,  100f,   0.0f, 0f,"x",                     "%01.0f","Cruise Control",      ""),
    )

    var list3E: Array<DIDStruct?>? = arrayOf(
        DIDStruct(0xd000ee2a, 1, false,-10f, 10f,    -1000f,1000f,  0.8f, 0f,"(x - 127) / 10",    "%05.3f","Accel. Lat",          "m/s2"),
        DIDStruct(0xd00141ba, 2, false,-10f, 10f,    -1000f,1000f,  0.4f, 0f,"(x - 512) / 32",    "%05.3f","Accel. Long",         "m/s2"),
        DIDStruct(0xd00097b4, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%05.3f","Airmass Actual",      "g/stk"),
        DIDStruct(0xd00097fc, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%05.3f","Airmass Setpoint",    "g/stk"),
        DIDStruct(0xd000c177, 1, false,-25f, 45f,    -100f, 100f,   0.9f, 0f,"(x - 64) / 1.33",   "%03.2f","Ambient Air Temp",    "°C"),
        DIDStruct(0xd0013c76, 2, false,0f,   110f,   0f,    120f,   0.9f, 0f,"x / 120.60176665",  "%03.2f","Ambient Pressure",    "kpa"),
        DIDStruct(0xd0015172, 2, true, 10f,  15f,    7f,    16f,    0.7f, 0f,"x / 51.2",          "%04.1f","Battery Volts",       "V"),
        DIDStruct(0xd000c36e, 1, false,-10f, 10f,    -100f, 100f,   0.0f, 0f,"x",                 "%05.2f","Combustion Mode",     ""),
        DIDStruct(0xd000c6f5, 1, false,-50f, 130f,   -100f, 150f,   0.8f, 0f,"(x - 64) / 1.33",   "%03.0f","Coolant Temp",        "°C"),
        DIDStruct(0xd001397a, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Limit",           "°"),
        DIDStruct(0xd0013982, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Actual",          "°"),
        DIDStruct(0xd0012400, 2, false,0f,   7000f,  -1.0f, 6000f,  0.0f, 0f,"x",                 "%04.0f","Engine Speed",        "rpm"),
        DIDStruct(0xd000c1d4, 1, false,-100f,100f,   -100f, 100f,   0.0f, 0f,"x / 2.55",          "%01.0f","Ethanol Content",     "%"),
        DIDStruct(0xd0011e04, 2, false,0f,   1.5f,   -1f,   10f,    0.0f, 0f,"x / 16384",         "%03.2f","Exhaust Flow Factor", ""),
        DIDStruct(0xd001566e, 2, true, -45f, 45f,    -100f, 100f,   0.0f, 0f,"x / 128",           "%04.1f","Exhaust Cam Position","°"),
        DIDStruct(0xd0011eba, 2, false,0f,   500f,   -100f, 1000f,  0.0f, 0f,"x / 120.60176665",  "%03.0f","Exhaust Pres Desired","kpa"),
        DIDStruct(0xd00135e0, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow Desired",   "mg/stk"),
        DIDStruct(0xd0013636, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow",           "mg/stk"),
        DIDStruct(0xd00192b1, 1, false,-100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Fuel Flow Split MPI", ""),
        DIDStruct(0xd0013600, 2, false,0f,   100f,   -1000f,100f,   0.0f, 0f,"x / 655.35999999",  "%01.0f","Fuel LPFP Duty",      "%"),
        DIDStruct(0xd0011b26, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%04.0f","Fuel LPFR Actual",    "kpa"),
        DIDStruct(0xd001360c, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%04.0f","Fuel LPFR Setpoint",  "kpa"),
        DIDStruct(0xd00136ac, 2, false,0f,   28000f, 0f,    28000f, 0.0f, 0f,"x / 1.884402603",   "%05.0f","Fuel HPFR Actual",    "kpa"),
        DIDStruct(0xd0013640, 2, false,0f,   28000f, -1000f,30000f, 0.0f, 0f,"x / 1.8844026039",  "%05.0f","Fuel HPFR Setpoint",  "kpa"),
        DIDStruct(0xd001363c, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999999",  "%01.0f","Fuel HPFP Volume",    "%"),
        DIDStruct(0xd000f00b, 1, false,-25f, 25f,    -20f,  20f,    0.8f, 0f,"x / 1.28 - 100",    "%01.0f","Fuel Trim Long Term", "%"),
        DIDStruct(0xd000f00c, 1, false,-25f, 25f,    -20f,  20f,    0.7f, 0f,"x / 1.28 - 100.0",  "%04.1f","Fuel Trim Short Term","%"),
        DIDStruct(0xd000f39a, 1, false,0f,   6f,     -1f,   7f,     0.0f, 0f,"x",                 "%02.2f","Gear",                "gear"),
        DIDStruct(0xd000e57e, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95) / 2.66667","%01.0f","Ignition Table Value","°"),
        DIDStruct(0xd000e59c, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95) / 2.66667","%01.0f","Ignition Timing",     "°"),
        DIDStruct(0xd001566c, 2, true, -100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Intake Cam Position", "°"),
        DIDStruct(0xd0013b16, 2, false,-25f, 25f,    -1000f,1000f,  0.0f, 0f,"x / 250",           "%02.2f","Injector PW DI",      "ms"),
        DIDStruct(0xd0013824, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 250",           "%05.1f","Injector PW MPI",     "ms"),
        DIDStruct(0xd000c179, 1, false,-50f, 70f,    -20f,  50f,    0.9f, 0f,"(x - 64) / 1.33",   "%03.2f","Intake Air Temp",     "°C"),
        DIDStruct(0xd0011e08, 2, false,0f,   20f,    -1000f,1000f,  0.0f, 0f,"x / 16384",         "%03.0f","Intake Flow Factor",  "-"),
        DIDStruct(0xd001988e, 1, false,0f,   -5f,    -3.0f, 3f,     0.8f, 0f,"(x - 128) / 2.66",  "%05.3f","Knock Retard",        "°"),
        DIDStruct(0xd000efb1, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 1",  "°"),
        DIDStruct(0xd000efb2, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 2",  "°"),
        DIDStruct(0xd000efb3, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 3",  "°"),
        DIDStruct(0xd000efb4, 1, false,0f,   -5f,    -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.6667","%04.2f","Knock Retard Cyl 4",  "°"),
        DIDStruct(0xd00120e2, 2, false,0.5f, 1.5f,   -0.1f, 5f,     0.0f, 0f,"x / 32767.999999",  "%04.2f","Lambda Actual",       "l"),
        DIDStruct(0xd00143f6, 2, false,0f,   2f,     -100f, 500f,   0.0f, 0f,"x / 1024",          "%03.2f","Lambda Setpoint",     "l"),
        DIDStruct(0xd00098cc, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000",          "%05.1f","MAP Actual",          "kpa"),
        DIDStruct(0xd00098f4, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000",          "%05.1f","MAP Setpoint",        "kpa"),
        DIDStruct(0xd000c5ae, 1, false,-25f, 120f,   -1000f,1000f,  0.9f, 0f,"x - 40",            "%01.0f","Oil Temp",            "°C"),
        DIDStruct(0xd000e578, 1, true, 0f,   220f,   -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.6667","%03.2f","openflex_cor",        "°CRK"),
        DIDStruct(0xd001de8d, 1, true, 0f,   7000f,  -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.6667","%06.1f","openflex_max_cor",    "°CRK"),
        DIDStruct(0xd001de8e, 1, false,0f,   3f,     -1000f,1000f,  0.0f, 0f,"x / 128",           "%05.3f","openflex_fac_cor",    ""),
        DIDStruct(0xd0012028, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 10.24",         "%04.1f","Pedal Position",      "%"),
        DIDStruct(0xd0000aa1, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Port Flap Position",  ""),
        DIDStruct(0xd00098fc, 4, false,0f,   300f,   -1f,   300f,   0.0f, 0f,"x / 1000",          "%05.1f","PUT Actual",          "kpa"),
        DIDStruct(0xd0011eee, 2, false,0f,   300f,   -1000f,1000f,  0.0f, 0f,"x / 120.6017666543","%05.1f","PUT Setpoint",        "kpa"),
        DIDStruct(0xd0013a44, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Actual",          "°"),
        DIDStruct(0xd0013a42, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Limit",           "°"),
        DIDStruct(0xd000f377, 1, false,0.5f, 1.3f,   -1000f,1000f,  0.0f, 0f,"x / 2.142128661087","%07.2f","Throttle Position",   "%"),
        DIDStruct(0xd0015344, 2, true, -40f, 500f,   -1000f,1000f,  0.0f, 0f,"x / 32",            "%07.2f","Torque Actual",       "Nm"),
        DIDStruct(0xd0011f0c, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Torque Limitation",   ""),
        DIDStruct(0xd0012048, 2, true, -40f, 500f,   -1000f,500f,   0.0f, 0f,"x / 32",            "%03.2f","Torque Requested",    "Nm"),
        DIDStruct(0xd000f3c1, 1, false,-100f,100f,   -1000f,120f,   0.9f, 0f,"x - 40",            "%01.0f","Transmission Temp",   "°C"),
        DIDStruct(0xd0011e76, 2, false,0f,   195000f,-100f, 190000f,0.0f, 0f,"x * 6.1035",        "%05.0f","Turbo Speed",         "rpm"),
        DIDStruct(0xd0019b75, 1, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Valve Lift Position", ""),
        DIDStruct(0xd00155b6, 2, false,0f,   220f,   -1000f,220f,   0.0f, 0f,"x / 100",           "%03.0f","Vehicle Speed",       "km/h"),
        DIDStruct(0xd0011e10, 2, false,-10f, 0f,     -1000f,1000f,  0.0f, 0f,"x / 655.35999997",  "%05.3f","Wastegate Actual",    "%"),
        DIDStruct(0xd0015c2c, 2, false,-10f, 0f,     -1000f,1000f,  0.0f, 0f,"x / 655.35999997",  "%05.3f","Wastegate Setpoint",  "%"),
        DIDStruct(0xd0015c5e, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 32",            "%03.1f","Wastegate Flow Req",  "kg/h"),
        DIDStruct(0xd001b6cd, 1, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Cruise Control",      ""),
    )

    fun list(): Array<DIDStruct?>? {
        when(UDSLogger.getMode()) {
            UDS_LOGGING_3E  -> return list3E
            UDS_LOGGING_22A -> return list22a
            UDS_LOGGING_22B -> return list22b
            UDS_LOGGING_22C -> return list22c
        }
        return list22a
    }

    fun getDID(address: Long): DIDStruct? {
        list()?.let { list ->
            for (i in 0 until list.count()) {
                list[i]?.let { did ->
                    if(did.address == address) {
                        return did
                    }
                }
            }
        }

        return null
    }

    fun setValue(did: DIDStruct?, x: Float): Float {
        if(did == null)
            return 0f

        //Used in smoothing calculation
        val previousValue = did.value

        //eval expression
        try {
            did.value = eval(did.equation.replace("x", x.toString(), true))
        } catch(e: Exception) {
            did.value = 0f
        }

        //Add smoothing
        if(did.smoothing > 0f && did.smoothing < 0.9751f)
            did.value = ((1f-did.smoothing) * did.value) + (did.smoothing * previousValue)

        return did.value
    }

    fun getValue(did: DIDStruct?): Float {
        if (did == null)
            return 0f

        return did.value
    }
}