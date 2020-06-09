# Ardublock-Standalone
Ardublock version that can run independent of Arduino IDE

To compile
- Linux: https://hackaday.io/project/166167-barnabas-robotics-ardublock-upgrade
- Windows: https://docs.google.com/document/d/1PI4S1P38JGV4XWac7j-tIMILE7Mu8z1q6YpewNNaw1A/edit

Resources
- JSerial & Maven: http://www.mschoeffler.de/2017/12/29/tutorial-serial-connection-between-java-application-and-arduino-uno/

Usage
- Arduino IDE needs to be installed but not running in order for the code to work since it runs using arduino_debug.exe

First Version
- Only tested on 64-bit Windows.
- Took previous version of Ardublock and created a standalone version by making calls to arduino_debug.exe and JSerial.
