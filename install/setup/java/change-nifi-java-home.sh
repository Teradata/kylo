#!/bin/bash

sed -i '/\#\!\/bin\/sh/a export JAVA_HOME=\/opt\/java\/current\nexport PATH=\$JAVA_HOME\/bin\:\$PATH' /opt/nifi/current/bin/nifi.sh
