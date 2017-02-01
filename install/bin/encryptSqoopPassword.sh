#!/bin/bash

JAVA_HOME="/opt/java/current"
LIB_JAR_LOCATION="/opt/kylo/lib/"
LIB_JAR_NAME="kylo-nifi-hadoop-processors-*.jar"
ENCRYPT_CLASS_NAME="com.thinkbiganalytics.nifi.v2.sqoop.security.EncryptPassword"

echo "*** Utility for generating encrypted password for use with Sqoop ***"
echo -n "Password to encrypt (Press Enter key when done): "
read -s password;
echo
echo -n "Passphrase (Press Enter key when done): "
read -s passPhrase;
echo
echo -n "Location to write the file to (Press Enter when done): "
read filePath;
echo
${JAVA_HOME}/bin/java -cp ${LIB_JAR_LOCATION}/${LIB_JAR_NAME} ${ENCRYPT_CLASS_NAME} $password $passPhrase $filePath
echo
echo "*** Done ***"