#!/bin/bash

KYLO_SETUP_FOLDER=/opt/kylo/setup

if [ $# -eq 1 ]
then
    KYLO_SETUP_FOLDER=$1
else
    echo "Unknown arguments. You must pass in the kylo setup folder location. For example: /opt/kylo/setup "
    exit 1
fi

mkdir ./nifi-kylo

mkdir -p ./nifi-kylo/lib/app

echo "Copying required files for HDF"
cp $KYLO_SETUP_FOLDER/nifi/*.nar ./nifi-kylo/lib
cp $KYLO_SETUP_FOLDER/nifi/kylo-spark-*.jar ./nifi-kylo/lib/app/

mkdir ./nifi-kylo/activemq
cp $KYLO_SETUP_FOLDER/nifi/activemq/*.jar ./nifi-kylo/activemq/

mkdir ./nifi-kylo/h2
mkdir ./nifi-kylo/ext-config
cp $KYLO_SETUP_FOLDER/nifi/config.properties ./nifi-kylo/ext-config/

mkdir ./nifi-kylo/feed_flowfile_cache

cp $KYLO_SETUP_FOLDER/nifi/hdf/install-kylo-hdf-components.sh ./nifi-kylo

tar -cvf nifi-kylo.tar ./nifi-kylo

rm -rf ./nifi-kylo

echo "The NiFi Kylo folder has been generated"