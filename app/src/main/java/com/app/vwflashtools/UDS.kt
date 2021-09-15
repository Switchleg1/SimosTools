package com.app.vwflashtools

//Equation list
//  9: ( 1.0 * X + 0.0 ) / 1000.0
// 10: ( 1.0 * X + 0.0 ) / 10.0

data class DIDStruct(val address: Int, val length: Int, val equation: Int, val name: String)

val DIDList: List<DIDStruct> = listOf(
    DIDStruct(0x2998, 2, 0, "Number_Of_Engine_Start_Manuell"),
    DIDStruct(0x2999, 2, 0, "Number_Of_Engine_Start_Automatic"),
    DIDStruct(0x4149, 2, 0, "Frequency_counter_deactivating_start_stop_function"),
    DIDStruct(0x39c0, 2, 9, "Intake_manifold_air_pressure_corrected_value"),
    DIDStruct(0x39c2, 2, 9, "Pressure_upstream_throttle_corrected_value"),
    DIDStruct(0x3d97, 2, 0, "Oxygen_sensor_1_bank_1_lambda_actual_value"),
    DIDStruct(0x3e0a, 2, 10, "Engine_coolant_temperature_sensor_engine_block_temperature_actual_raw_value"),)

class UDS {

}

//0x11.toByte(), 0xF7.toByte(),   //PID0E Ignition timing advance for logical cylinder 0,A_UINT32,1,( 0.5 * X + -64.0 ) / 1
//newPIDS[3] = bData[15].toUnsigned().toFloat() * 0.5.toFloat() - 64.0.toFloat()