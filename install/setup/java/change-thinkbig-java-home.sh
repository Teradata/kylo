#!/bin/bash

sed -i '/\#\!\/bin\/bash/a export JAVA_HOME=\/opt\/java\/current\nexport PATH=\$JAVA_HOME\/bin\:\$PATH' /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services.sh
sed -i '/\#\!\/bin\/bash/a export JAVA_HOME=\/opt\/java\/current\nexport PATH=\$JAVA_HOME\/bin\:\$PATH' /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services-with-debug.sh
sed -i '/\#\!\/bin\/bash/a export JAVA_HOME=\/opt\/java\/current\nexport PATH=\$JAVA_HOME\/bin\:\$PATH' /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui.sh
sed -i '/\#\!\/bin\/bash/a export JAVA_HOME=\/opt\/java\/current\nexport PATH=\$JAVA_HOME\/bin\:\$PATH' /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui-with-debug.sh