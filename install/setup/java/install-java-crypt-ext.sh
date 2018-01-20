#!/bin/bash
#########
#  Example 1: ./install-java-crypt-ext.sh /opt/java
#  Example 2: ./install-java /opt/java /tmp/offline-install -o
#########

#set -x

offline=false
java_dir=/opt/java
KYLO_SETUP_FOLDER=$2

if [ $# -eq 0 ]
then
    echo "No setup folder specified. Defaulting to the java home to /opt/java"
elif [ $# -eq 1 ]
then
    echo "The java home folder is $1 "
    java_dir=$1
elif [ $# -eq 3 ] && ([ "$3" = "-o" ] || [ "$3" = "-O" ])
then
    echo "Working in offline mode"
    offline=true
    java_dir=$1
else
    echo "Unknown arguments. The first argument should be the path to the kylo home folder. Optional you can pass a second argument to set offline mode. The value is -o or -O "
    exit 1
fi

sec_dir=$java_dir/jre/lib/security

if ! [ -d $sec_dir ]
then
    echo "No java security directory found at: $sec_dir... exiting"
    exit 1
fi

echo "Installing Java 8 Java Cryptography Extension into $sec_dir"
cd $sec_dir

if [ $offline = true ]
then
    cp $KYLO_SETUP_FOLDER/java/jce_policy-8.zip .
else
    curl -L -O -H  "Cookie: oraclelicense=accept-securebackup-cookie" -k "http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip"
fi

if ! [ -f jce_policy-8.zip ]
then
    echo "Working in offline mode and file not found... exiting"
    exit 1
fi

cp local_policy.jar local_policy.jar$(date "+-%FT%T")
cp US_export_policy.jar US_export_policy.jar$(date "+-%FT%T")
unzip -oj jce_policy-8.zip
rm -f jce_policy-8.zip
