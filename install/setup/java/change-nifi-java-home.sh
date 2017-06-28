#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "You must pass in a JAVA_HOME path followed by the NIFI_HOME path"
    exit 1
fi

echo "Adding custom Java path to the NiFi startup script"
cat >>$2/bin/nifi-env.sh <<EOF

export JAVA_HOME=$1
export PATH=\$JAVA_HOME/bin:\$PATH
EOF
