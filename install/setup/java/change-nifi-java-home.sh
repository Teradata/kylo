#!/bin/bash

echo "Adding custom Java path to the NiFi startup script"
sed -i '/\#\!\/bin\/sh/a export JAVA_HOME=\/opt\/java\/current\nexport PATH=\$JAVA_HOME\/bin\:\$PATH' /opt/nifi/current/bin/nifi.sh
