# VWFlashTools

Will use the BLE ISOTP bridge by bri3d to log and flash Simo ECUs https://github.com/Switchleg1/esp32-isotp-ble-bridge/tree/BridgeLEG

Currently can make connection and log a set of PIDS from the ECU using mode 22 and save to .csv using the cruise control button as trigger.

Simple example of logging capability:
https://datazap.me/u/switchleg/log-1632161044?log=0&data=2-3

PIDs can be modified in the config file including the min/max for realtime display.

Equation list for PID config file 				<br />
	0: none 																<br />
	1: ( 0.375 * X + -48.0 ) / 1						<br />
  2: ( 1.0 * X + -2731.4 ) / 10.0					<br />
  3: ( 1.0 * X + 0.0 ) / 1.28f - 100.0f		<br />
  4: ( 6.103515624994278 * X + 0.0 ) / 1	<br />
  5: ( 0.0078125 * X + -0.0 ) / 1					<br />
  6: ( 1.0 * X + 0.0 ) / 4.0							<br />
  7: ( 0.75 * X + -48.0 ) / 1							<br />
  8: ( 1.0 * X + 0.0 ) / 10.0							<br />
  9: ( 1.0 * X + 0.0 ) / 100.0						<br />
  10: ( 1.0 * X + 0.0 ) / 1000.0					<br />
  11: ( 0.03125 * X + 0.0 ) / 1						<br />
  12: ( 0.08291752498664835 * X + 0.0 ) / 1000.0	<br />
  13: X * 100.0 / 255.0										<br />
  14: X / 2.0 – 64.0											<br />
  15: ( 0.0009765625 * X + 0.0 ) / 1			<br />
  16: X / 2.4															<br />
  17: X * 0.005														<br />
  18: X * 0.002874												<br />
  19: 100 - ( 1.0 * X + 0.0 ) / 100.0			<br />
