1) Make sure to install JDK 14
2) add java to JAVA_HOME environment variable
3) add JAVA_HOME to path

For Mac

FOR PKG:
jpackage --input target_mac/ --name BarnabasArdublock --main-jar ardublock-all.jar --main-class com.ardublock.Main --type pkg --java-options '--enable-preview' --icon "barnabas_logo.icns"


For Windows

The command below creates installer that will install software into Program Files folder along with dependencies

jpackage --input target_win/ --name BarnabasArdublock --main-jar ardublock-all.jar --main-class com.ardublock.Main --type exe --java-options '--enable-preview' --icon "barnabas_logo.ico" --win-shortcut
