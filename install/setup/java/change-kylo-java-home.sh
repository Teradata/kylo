#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "You must pass in a JAVA_HOME path"
    exit 1
fi

echo "Adding Custom Java home to kylo-ui and kylo-services"
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/kylo/kylo-services/bin/run-kylo-services.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/kylo/kylo-services/bin/run-kylo-services-with-debug.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/kylo/kylo-ui/bin/run-kylo-ui.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$1\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" /opt/kylo/kylo-ui/bin/run-kylo-ui-with-debug.sh