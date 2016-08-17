#!/bin/bash

rm -rf /opt/nifi/data/lib/*.nar
rm -rf rm -rf /opt/nifi/data/lib/app/*.jar

cp /opt/thinkbig/setup/nifi/*.nar /opt/nifi/data/lib
cp /opt/thinkbig/setup/nifi/thinkbig-spark-*.jar /opt/nifi/data/lib/app

chown -R nifi:users /opt/nifi/data/lib

./create-symbolic-links.sh