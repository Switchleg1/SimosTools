package com.app.vwflashtools

//Equation list
//  0: none
//  1: ( 0.375 * X + -48.0 ) / 1
//  2: ( 1.0 * X + -2731.4 ) / 10.0
//  3: ( 1.0 * X + 0.0 ) / 1.28f - 100.0f
//  4: ( 6.103515624994278 * X + 0.0 ) / 1
//  5: ( 0.0078125 * X + -0.0 ) / 1
//  6: ( 1.0 * X + 0.0 ) / 4.0
//  7: ( 0.75 * X + -48.0 ) / 1
//  8: ( 1.0 * X + 0.0 ) / 10.0
//  9: ( 1.0 * X + 0.0 ) / 100.0
// 10: ( 1.0 * X + 0.0 ) / 1000.0

data class DIDStruct(val address: Int, val length: Int, val equation: Int, val name: String, val unit: String)

val DIDList: List<DIDStruct> = listOf(
    DIDStruct(0x1001, 1, 7, "Intake air temperature", "°C"),
    DIDStruct(0x1040, 2, 7, "Turbo charger rotational speed", "rpm"),
    DIDStruct(0x13f2, 1, 1, "Total spark retard cylinder 1", "°"),
    DIDStruct(0x13f3, 1, 1, "Total spark retard cylinder 2", "°"),
    DIDStruct(0x13f4, 1, 1, "Total spark retard cylinder 3", "°"),
    DIDStruct(0x13f5, 1, 1, "Total spark retard cylinder 4", "°"),
    DIDStruct(0x15d3, 2, 5, "Filtered vehicle speed", "km/hr"),
    DIDStruct(0x203c, 2, 0, "Cruise control status", ""),
    DIDStruct(0x203f, 2, 8, "Engine torque limitation", "Nm"),
    DIDStruct(0x206d, 2, 9, "Throttle_valve_control_value", "%"),
    DIDStruct(0x39a9, 2, 9, "ignition angle", "°"),
    DIDStruct(0x39c0, 2, 10, "Intake_manifold_air_pressure_corrected_value","bar"),
    DIDStruct(0x39c2, 2, 10, "Pressure_upstream_throttle_corrected_value","bar"),
    DIDStruct(0x3d97, 2, 0, "Oxygen_sensor_1_bank_1_lambda_actual_value","l"),
    DIDStruct(0x3e0a, 2, 9, "Engine_coolant_temperature_sensor_engine_block_temperature_actual_raw_value","°C"),
    DIDStruct(0xF406, 1, 3, "Short Term Fuel Trim - Bank 1","%"),
    DIDStruct(0xF40C, 2, 6, "Engine RPM","rpm"),
    DIDStruct(0x202f, 2, 2, "Engine oil temperature", "°C"),
)

object DIDClass {
    fun getDID(address: Int): DIDStruct? {
        for (i in 0 until DIDList.count()) {
            if(DIDList[i].address == address) {
                return DIDList[i]
            }
        }
        return null
    }

    fun getValue(did: DIDStruct, data: Float): Float {
        when(did.equation) {
            0 -> {
                return data
            }
            1 -> {
                return 0.375f * data - 48.0f
            }
            2 -> {
                return 0.375f * data - 48.0f
            }
            3 -> {
                return data / 1.28f - 100.0f
            }
            4 -> {
                return 6.1035f * data
            }
            5 -> {
                return 0.0078125f * data
            }
            6 -> {
                return data / 4.0f
            }
            7 -> {
                return 0.75f * data - 48.0f
            }
            8 -> {
                return data / 10.0f
            }
            9 -> {
                return data / 100.0f
            }
            10 -> {
                return data / 1000.0f
            }
        }

        return data
    }
}