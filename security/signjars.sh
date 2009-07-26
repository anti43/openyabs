#!/bin/bash
echo 'Start unsigning the JARs';
find ../dist -name "*.jar" -exec ./unsignjars.sh '{}' \;
echo 'Start signing the JARs';
find ../dist -name "*.jar" -exec jarsigner -keystore mpv5store -keypass password -storepass password '{}' mpv5key \;
echo 'JARs signed';
exit 0 
