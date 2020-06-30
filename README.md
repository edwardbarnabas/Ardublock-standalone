# Ardublock-Standalone

![Screenshot](/images/ardublock_20200625.jpg)

This project is a branch from the original Ardublock project (link below) from @taweili.  We plan to use this with our robotics students, but it can be adapted to any Arduino-based platform.

## Usage:
- Installers for mac/windows can be found in the "bundler" folder.

## General Software Architecture:
- Uses JSerialComm library to access comm ports for detection and serial monitor
- Uses arduino-cli to compile and upload
- Uses open blocks to generate and translate blocks into C code

## Blocks, Project Selection and XML:
- The blocks generated and shown are set by the .xml file in: ardublock-all/src/main/resources/com/ardublock/block/
- Right now, there are two different sets of blocks based on the project selected.  Barnabas-Bot -> ardublock_barnabas_bot.xml.  Barnabas-Racer -> ardublock_barnabas_racer.xml
- To add more blocks, I recommend creating a new .xml file and add it is an option that is selected when users select the "Project".  ardublock.xml is the original xml file that can be referenced when making your own .xml.

## Creating New Blocks:
- To create new blocks, you need to modify three things.
1) ardublock_<project>.xml - controls what the blocks look like, and what is shown in the GUI.  Found in: ardublock-all/src/main/resources/com/ardublock/block/
2) <block>.java - class that you create to translate block into C code.  Placed in: ardublock-all/src/main/java/com/ardublock/translator/block/
3) block-mapping.properties - connects the xml and .java file. Found in: ardublock-all/src/main/resources/com/ardublock/block/

## Compiling:
- Linux: https://hackaday.io/project/166167-barnabas-robotics-ardublock-upgrade
- Windows: https://docs.google.com/document/d/1PI4S1P38JGV4XWac7j-tIMILE7Mu8z1q6YpewNNaw1A/edit

## Creating .EXE and .PKG:
- Run build_exe.bat on a Windows machine to create Windows installer
- Run build_pkg.sh on a Mac machine to create a Mac installer.

## Resources:
- JSerial & Maven: http://www.mschoeffler.de/2017/12/29/tutorial-serial-connection-between-java-application-and-arduino-uno/

## Notes:
- 20200626: Added support of arduino-cli and created scripts for mac and windows installer generation.
- 20200619: Linux path added to support linux.  Updated GUI.  Allow user to select Barnabas project to dynamically change available blocks shown.
- 20200614: Now works with Mac
- 20200610: Only tested on 64-bit Windows.
- 20200610: Took previous version of Ardublock and created a standalone version by making calls to arduino_debug.exe and JSerial.

## Credits:
- Original Ardublock project: https://github.com/taweili/ardublock
- Ardublock hacking Vinctronics: https://hackaday.io/project/166167-barnabas-robotics-ardublock-upgrade/log/164855-vincents-ardublock-contributions
- Ardublock 2019 BIG Group: https://github.com/BarnabasRobotics/BarnabasRoboticsArdublock-2019
