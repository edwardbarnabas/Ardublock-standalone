: CD to ardublock-all directory
cd ardublock-all

: Create clean maven package
call mvn clean
call mvn package

: CD back to ardublock-standalone directory
cd..

: Move new ardubock.jar file to the bundler
move /y %CD%\ardublock-all\target\ardublock-all.jar %CD%\bundler\target_win

: CD to bundler directory
cd bundler

: Build package
call jpackage --input target_win/ --name BarnabasArdublock --main-jar ardublock-all.jar --main-class com.ardublock.Main --type exe --java-options '--enable-preview' --icon "barnabas_logo.ico" --win-shortcut --win-menu --app-version 1.0

: Pause to allow user to see terminal feedback
pause