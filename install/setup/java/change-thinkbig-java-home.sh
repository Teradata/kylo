#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "You must pass in a JAVA_HOME path"
    exit 1
fi

echo "Adding Custom Java home to thinkbig-ui and thinkbig-services"
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/thinkbig/thinkbig-services/bin/run-thinkbig-services-with-debug.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/thinkbig/thinkbig-ui/bin/run-thinkbig-ui-with-debug.sh