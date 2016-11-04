#!/bin/bash

#set -x

offline=false
java_dir=$1
sec_dir=$java_dir/jre/lib/security
working_dir=$3

if [ $# > 1 ]
then
    if [ "$2" = "-o" ] || [ "$2" = "-O" ]
    then
        echo "Working in offline mode"
        offline=true
    fi
fi

if ! [ -d $sec_dir ]
then
    echo "No java security directory found at: $sec_dir... exiting"
    exit 1
fi

echo "Installing Java 8 Java Cryptography Extension into $sec_dir"
cd $sec_dir

if [ $offline = true ]
then
    cp $working_dir/java/jce_policy-8.zip .
else
    curl -L -O -H "Cookie: oraclelicense=accept-securebackup-cookie" -k "http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip"
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
