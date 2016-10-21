#!/bin/bash

echo "Please enter the password you want to encrypt";
read -p "> " -s password ;
echo "Please enter the passphrase";
read -p "> " -s passPhrase;
echo "Please enter the location to write the file to" ;
read -p "> " fileLocation  

/opt/java/jdk1.8.0_92/bin/java -cp /opt/thinkbig/lib/thinkbig-nifi-hadoop-processors-*.jar com.thinkbiganalytics.nifi.v2.sqoop.security.EncryptPassword $password $passPhrase $fileLocation

echo "fixing git"
