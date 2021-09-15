package com.app.vwflashtools

class UDSThread: Thread() {

}

//0x11.toByte(), 0xF7.toByte(),   //PID0E Ignition timing advance for logical cylinder 0,A_UINT32,1,( 0.5 * X + -64.0 ) / 1
//newPIDS[3] = bData[15].toUnsigned().toFloat() * 0.5.toFloat() - 64.0.toFloat()