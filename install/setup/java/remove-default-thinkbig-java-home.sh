#!/bin/bash

echo "Removing custom JAVA_HOME from thinkbig-ui and thinkbig-services"
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services-with-debug.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services-with-debug.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui-with-debug.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui-with-debug.sh