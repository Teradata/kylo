#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "You must pass in a NIFI_HOME path"
    exit 1
fi

NIFI_HOME=$1

echo "Removing custom JAVA_HOME from nifi start script"
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' $NIFI_HOME/bin/nifi.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' $NIFI_HOME/bin/nifi.sh