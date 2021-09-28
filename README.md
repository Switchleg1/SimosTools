# VWFlashTools

Will use the BLE ISOTP bridge by bri3d to log and flash Simo ECUs https://github.com/Switchleg1/esp32-isotp-ble-bridge/tree/BridgeLEG

Currently can make connection and log a set of PIDS from the ECU using mode 22 or 3E and saves to .csv using the cruise control button as trigger.

Simple example of logging capability:<br />
https://datazap.me/u/switchleg/log-1632161044?log=0&data=2-3<br />
https://datazap.me/u/switchleg/log-1632680629?log=0&data=2-3<br />

PIDs can be modified in the config file including the min/max for realtime display.

Equation list for PID config file<br />
//  0: none<br />
//  1: X * 1000<br />
//  2: (X - 2731.4) / 10.0<br />
//  3: (X / 1.28f) - 100.0f<br />
//  4: X * 6.103515624994278<br />
//  5: X * 0.0078125<br />
//  6: X / 4.0<br />
//  7: (X * 0.75) -48.0<br />
//  8: X / 10.0<br />
//  9: X / 100.0<br />
// 10: X / 1000.0<br />
// 11: X / 100000.0<br />
// 12: X * 0.08291752498664835 / 1000.0<br />
// 13: X / 51.2<br />
// 14: X / 47.18142548596112<br />
// 15: X * 0.0009765625<br />
// 16: X / 2.4<br />
// 17: X * 0.005<br />
// 18: X * 0.002874<br />
// 19: 100 - (X / 100.0)<br />
// 20: X / 2.66666666666667<br />
// 21: (X - 95) / 2.66666666666667<br />
// 22: X / 655.3599999999997<br />
// 23: X / 2.55<br />
// 24: X / 16384<br />
// 25: X / 0.3768805207949945<br />
// 26: X / 2.142128661087866<br />
// 27: (X - 128) / 2.66666666666667<br />
// 28: (X - 64) / 1.33333333333333<br />
// 29: X / 10.24<br />
// 30: X - 40<br />
// 31: X / 128<br />
// 32: X / 12.06017666543982<br />
// 33: X / 32767.99999999992<br />
// 34: X / 1024<br />
// 35: X / 250<br />
// 36: X / 0.1884402603974972<br />
// 37: X * 0.03125<br />
