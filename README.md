# Ardublock-Standalone

![Screenshot](/images/ardublock_20200625.jpg)

This project is a branch from the original Ardublock project (link below) from @taweili.  We plan to use this with our robotics students, but it can be adapted to any Arduino-based platform.  Arduino must be install on a system for it it t work.  

Usage:
- Installers for mac/windows can be found in the "bundler" folder.

General Software Architecture:
- Uses JSerialComm library to access comm ports for detection and serial monitor
- Uses arduino-cli to compile and upload
- Uses open blocks to generate and translate blocks into C code

Notes on compiling:
- Linux: https://hackaday.io/project/166167-barnabas-robotics-ardublock-upgrade
- Windows: https://docs.google.com/document/d/1PI4S1P38JGV4XWac7j-tIMILE7Mu8z1q6YpewNNaw1A/edit

Creating .EXE and .PKG:
- Run build_exe.bat on a Windows machine to create Windows installer
- Run build_pkg.sh on a Mac machine to create a Mac installer.

Resources:
- JSerial & Maven: http://www.mschoeffler.de/2017/12/29/tutorial-serial-connection-between-java-application-and-arduino-uno/

Notes:
- 20200626: Added support of arduino-cli and created scripts for mac and windows installer generation.
- 20200619: Linux path added to support linux.  Updated GUI.  Allow user to select Barnabas project to dynamically change available blocks shown.
- 20200614: Now works with Mac
- 20200610: Only tested on 64-bit Windows.
- 20200610: Took previous version of Ardublock and created a standalone version by making calls to arduino_debug.exe and JSerial.

Credits:
- Original Ardublock project: https://github.com/taweili/ardublock
- Ardublock hacking Vinctronics: https://hackaday.io/project/166167-barnabas-robotics-ardublock-upgrade/log/164855-vincents-ardublock-contributions
- Ardublock 2019 BIG Group: https://github.com/BarnabasRobotics/BarnabasRoboticsArdublock-2019
