# Ardublock-Standalone

![](./images/SharedScreenShot.jpg)

This project is a branch from the original Ardublock project (link below) from @taweili.  We plan to use this with our robotics students, but it can be adapted to any Arduino-based platform.  Arduino must be install on a system for it it t work.  

General Software Architecture:
- Uses JSerialComm library to access comm ports for detection and serial monitor
- Uses Arduino command line tools to compile and upload code (Windows: arduino_debug.exe, Mac: /Applications/Arduino.app/Contents/MacOS/Arduino Linx: TBD)

Notes on compiling:
- Linux: https://hackaday.io/project/166167-barnabas-robotics-ardublock-upgrade
- Windows: https://docs.google.com/document/d/1PI4S1P38JGV4XWac7j-tIMILE7Mu8z1q6YpewNNaw1A/edit

Resources:
- JSerial & Maven: http://www.mschoeffler.de/2017/12/29/tutorial-serial-connection-between-java-application-and-arduino-uno/

Usage:
- Arduino IDE needs to be installed but not running in order for the code to work since it runs using arduino_debug.exe

Notes:
- 20200614: Now works with Mac
- 20200610: Only tested on 64-bit Windows.
- 20200610: Took previous version of Ardublock and created a standalone version by making calls to arduino_debug.exe and JSerial.

Credits:
- Original Ardublock project: https://github.com/taweili/ardublock
- Ardublock hacking Vinctronics: https://hackaday.io/project/166167-barnabas-robotics-ardublock-upgrade/log/164855-vincents-ardublock-contributions
- Ardublock 2019 BIG Group: https://github.com/BarnabasRobotics/BarnabasRoboticsArdublock-2019
