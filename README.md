# VWFlashTools

Will use the BLE ISOTP bridge by bri3d to log and flash Simo ECUs https://github.com/Switchleg1/esp32-isotp-ble-bridge/tree/BridgeLEG

Currently can make connection and log a set of PIDS from the ECU using mode 22 or 3E and saves to .csv using the cruise control button as trigger.

Simple example of logging capability:<br />
https://datazap.me/u/switchleg/log-1632161044?log=0&data=2-3<br />
https://datazap.me/u/switchleg/log-1632680629?log=0&data=2-3<br />

PIDs can be modified in the config file including the min/max for realtime display.

Equation list for PID config file<br />
//  0: x <br />
//  1: x * 1000<br />
//  2: (x - 2731.4) / 10.0<br />
//  3: x / 1.28 - 100.0<br />
//  4: x * 6.103515624994278<br />
//  5: x / 128<br />
//  6: x / 4.0<br />
//  7: x * 0.75 - 48.0<br />
//  8: x / 10.0<br />
//  9: x / 100.0<br />
// 10: x / 1000.0<br />
// 11: x / 100000.0<br />
// 12: x / 32<br />
// 13: x / 51.2<br />
// 14: x / 47.18142548596112<br />
// 15: x / 10.24<br />
// 16: x / 1024<br />
// 17: x / 2.4<br />
// 18: x / 200<br />
// 19: x / 347.94711203897007654836464857342<br />
// 20: 100.0 - x / 100.0<br />
// 21: x / 2.66666666666667<br />
// 22: (x - 95.0f) / 2.66666666666667<br />
// 23: x / 655.3599999999997<br />
// 24: x / 2.55<br />
// 25: x / 16384<br />
// 26: x / 250<br />
// 27: x / 2.142128661087866<br />
// 28: (x - 128) / 2.66666666666667<br />
// 29: (x - 64) / -1.33333333333333<br />
// 30: x - 40<br />
// 31: x / 3.768805207949945<br />
// 32: x / 376.8805207949945<br />
// 33: x / 32767.99999999992<br />
// 34: x / 188.4402603974972<br />
// 35: x / 120.6017666543982<br />
// 36: x / 12060.17666543982<br />
// 37: x / 1.884402603974972<br />
// 38: x * 10<br />
