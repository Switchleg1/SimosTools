# VWFlashTools

Will use the BLE ISOTP bridge by bri3d to log and flash Simo ECUs https://github.com/bri3d/esp32-isotp-ble-bridge

Currently can make connection, pull a set of PIDS from the ECU and save to .csv using the cruise control button as trigger.

https://datazap.me/u/switchleg/log-1632161044?log=0&data=2-3

Equation list for PID config file
  0: none
  1: ( 0.375 * X + -48.0 ) / 1
  2: ( 1.0 * X + -2731.4 ) / 10.0
  3: ( 1.0 * X + 0.0 ) / 1.28f - 100.0f
//  4: ( 6.103515624994278 * X + 0.0 ) / 1
//  5: ( 0.0078125 * X + -0.0 ) / 1
//  6: ( 1.0 * X + 0.0 ) / 4.0
//  7: ( 0.75 * X + -48.0 ) / 1
//  8: ( 1.0 * X + 0.0 ) / 10.0
//  9: ( 1.0 * X + 0.0 ) / 100.0
// 10: ( 1.0 * X + 0.0 ) / 1000.0
// 11: ( 0.03125 * X + 0.0 ) / 1
// 12: ( 0.08291752498664835 * X + 0.0 ) / 1000.0
// 13: X * 100.0 / 255.0
// 14: X / 2.0 – 64.0
// 15: ( 0.0009765625 * X + 0.0 ) / 1
// 16: X / 2.4
// 17: X * 0.005
// 18: X * 0.002874
// 19: 100 - ( 1.0 * X + 0.0 ) / 100.0
