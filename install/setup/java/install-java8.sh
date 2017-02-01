#!/bin/bash

#set -x

MY_DIR=$(dirname $0)

offline=false
working_dir=$2
KYLO_INSTALL_HOME=/opt/kylo

if [ $# > 1 ]
then
    if [ "$1" = "-o" ] || [ "$1" = "-O" ]
    then
        echo "Working in offline mode"
        offline=true
    fi
fi

if [ ${offline} = true ]
then
    JAVA_SETUP_DIR=${working_dir}/java
else
    JAVA_SETUP_DIR=${KYLO_INSTALL_HOME}/setup/java
fi

echo "Installing Java 8 in /opt/java"
cd /opt
mkdir java
cd java

if [ $offline = true ]
then
    cp $working_dir/java/jdk-8u92-linux-x64.tar.gz .
else
    curl -L -O -H "Cookie: oraclelicense=accept-securebackup-cookie" -k "http://download.oracle.com/otn-pub/java/jdk/8u92-b14/jdk-8u92-linux-x64.tar.gz"
fi

if ! [ -f jdk-8u92-linux-x64.tar.gz ]
then
    echo "Working in offline mode and file not found.. exiting"
    exit 1
fi

tar -xvf jdk-8u92-linux-x64.tar.gz
rm -f jdk-8u92-linux-x64.tar.gz
echo "Creating symbolic link called 'current' to simplify upgrades"
ln -s jdk1.8.0_92 current

if [ ${offline} = true ]
then
    ${JAVA_SETUP_DIR}/install-java-crypt-ext.sh /opt/java/current -O ${working_dir}
else
    ${JAVA_SETUP_DIR}/install-java-crypt-ext.sh /opt/java/current
fi
