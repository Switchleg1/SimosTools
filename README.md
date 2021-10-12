# Simos Logger

Will use the BLE ISOTP bridge by bri3d to log Simo ECUs https://github.com/Switchleg1/esp32-isotp-ble-bridge/tree/BridgeLEG

Currently can make connection and log a set of configurable PIDS from the ECU using mode 22 or 3E and saves to .csv using the cruise control button as trigger.

Simple example of logging capability:<br />
https://datazap.me/u/switchleg/log-1632161044?log=0&data=2-3<br />
https://datazap.me/u/switchleg/log-1632680629?log=0&data=2-3<br />

PIDs can be modified in the config file including the min/max for realtime display.
