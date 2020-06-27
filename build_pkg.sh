: CD to ardublock-all directory
cd ardublock-all

: Create clean maven package
mvn clean
mvn package

: CD back to ardublock-standalone directory
cd ..

: Move new ardubock.jar file to the bundler
mv -f $PWD/ardublock-all/target/ardublock-all.jar $PWD/bundler/target_mac

: CD to bundler directory
cd bundler

: Build package
jpackage --input target_mac/ --name BarnabasArdublock --main-jar ardublock-all.jar --main-class com.ardublock.Main --type pkg --java-options '--enable-preview' --icon "barnabas_logo.icns" --app-version 1.0