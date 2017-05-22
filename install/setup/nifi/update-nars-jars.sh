#!/bin/bash

NIFI_HOME=$1
KYLO_SETUP_FOLDER=$2
NIFI_USER=$3
NIFI_GROUP=$4

if [ $# -eq 4 ]
then
    echo "The NIFI home folder is $NIFI_HOME using permissions  $NIFI_USER:$NIFI_GROUP"
else
    echo "Unknown arguments. You need to pas NIFI_HOME KYLO_SETUP_FOLDER NIFI_USER NIFI_GROUP "
    exit 1
fi

MY_DIR=$(dirname $0)

echo "Updating the kylo nifi nar and jar files"
rm -rf $NIFI_HOME/data/lib/*.nar
rm -rf $NIFI_HOME/data/lib/app/*.jar

cp $KYLO_SETUP_FOLDER/nifi/*.nar $NIFI_HOME/data/lib
cp $KYLO_SETUP_FOLDER/nifi/kylo-spark-*.jar $NIFI_HOME/data/lib/app

chown -R nifi:users $NIFI_HOME/data/lib

${MY_DIR}/create-symbolic-links.sh $NIFI_HOME $NIFI_USER $NIFI_GROUP

echo "Nar files and Jar files have been updated"
