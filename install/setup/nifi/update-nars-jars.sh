#!/bin/bash

rm -rf /opt/nifi/data/lib/*.nar
rm -rf rm -rf /opt/nifi/data/lib/app/*.jar

cp $NIFI_SETUP_DIR/*.nar /opt/nifi/data/lib
cp $NIFI_SETUP_DIR/thinkbig-spark-*.jar /opt/nifi/data/lib/app

chown -R nifi:users /opt/nifi/data/lib

./create-symbolic-links.sh