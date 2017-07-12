#!/bin/bash
#########
#  Example 1: ./install-java /opt/kylo-dev
#  Example 2: ./install-java /opt/kylo-dev /tmp/offline-install -o
#########

#set -x

MY_DIR=$(dirname $0)

offline=false
OFFLINE_SETUP_FOLDER=$2
KYLO_INSTALL_HOME=/opt/kylo
JAVA_INSTALL_FOLDER=/opt/java

JDK_FOLDER_NAME=jdk1.8.0_131
JDK_FILE_NAME=jdk-8u131-linux-x64.tar.gz
JDK_DOWNLOAD_URL=http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz

if [ $# -eq 0 ]
then
    echo "No setup folder specified. Defaulting to kylo home to /opt/kylo"
elif [ $# -eq 1 ]
then
    echo "The kylo home folder is $1 "
    KYLO_INSTALL_HOME=$1
elif [ $# -eq 3 ] && ([ "$3" = "-o" ] || [ "$3" = "-O" ])
then
    echo "Working in offline mode"
    offline=true
    KYLO_INSTALL_HOME=$1
else
    echo "Unknown arguments. The first argument should be the path to the kylo home folder. Optional you can pass a second argument to set offline mode. The value is -o or -O "
    exit 1
fi


if [ ${offline} = true ]
then
    JAVA_SETUP_DIR=${OFFLINE_SETUP_FOLDER}/java
else
    JAVA_SETUP_DIR=${KYLO_INSTALL_HOME}/setup/java
fi

echo "Installing Java 8 in $JAVA_INSTALL_FOLDER"
mkdir -p $JAVA_INSTALL_FOLDER
cd $JAVA_INSTALL_FOLDER

if [ $offline = true ]
then
    cp $OFFLINE_SETUP_FOLDER/java/jdk-*.gz .
else
    wget -c -O "$JDK_FILE_NAME" --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" "$JDK_DOWNLOAD_URL"
fi

if ! [ -f $JDK_FILE_NAME ]
then
    echo "Working in offline mode and file not found.. exiting"
    exit 1
fi

tar -xvf $JDK_FILE_NAME
rm -f $JDK_FILE_NAME
echo "Creating symbolic link called 'current' to simplify upgrades"
ln -s $JDK_FOLDER_NAME current

if [ ${offline} = true ]
then
    ${JAVA_SETUP_DIR}/install-java-crypt-ext.sh $JAVA_INSTALL_FOLDER/current ${OFFLINE_SETUP_FOLDER} -O
else
    ${JAVA_SETUP_DIR}/install-java-crypt-ext.sh $JAVA_INSTALL_FOLDER/current
fi
