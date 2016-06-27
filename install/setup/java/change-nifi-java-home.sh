#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "You must pass in a JAVA_HOME path"
    exit 1
fi

echo "Adding custom Java path to the NiFi startup script"
sed -i "/\#\!\/bin\/sh/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/nifi/current/bin/nifi.sh
