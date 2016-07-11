#!/bin/bash

echo "Removing custom JAVA_HOME from nifi start script"
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/nifi/current/bin/nifi.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/nifi/current/bin/nifi.sh