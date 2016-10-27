#!/bin/bash

MY_DIR=$(dirname $0)

echo "Updating the thinkbig nifi nar and jar files"
rm -rf /opt/nifi/data/lib/*.nar
rm -rf /opt/nifi/data/lib/app/*.jar

cp /opt/thinkbig/setup/nifi/*.nar /opt/nifi/data/lib
cp /opt/thinkbig/setup/nifi/thinkbig-spark-*.jar /opt/nifi/data/lib/app

chown -R nifi:users /opt/nifi/data/lib

${MY_DIR}/create-symbolic-links.sh

echo "Nar files and Jar files have been updated"
