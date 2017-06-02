#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "You must pass in a JAVA_HOME path followed by the kylo home path"
    exit 1
fi

JAVA_HOME=$1
KYLO_HOME=$2

echo "Adding Custom Java home to kylo-ui and kylo-services"
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$JAVA_HOME\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" $KYLO_HOME/kylo-services/bin/run-kylo-services.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$JAVA_HOME\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" $KYLO_HOME/kylo-services/bin/run-kylo-services-with-debug.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$JAVA_HOME\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" $KYLO_HOME/kylo-ui/bin/run-kylo-ui.sh
sed -i "/\#\!\/bin\/bash/a export JAVA_HOME=$JAVA_HOME\nexport PATH=\$JAVA_HOME\/bin\:\$PATH" $KYLO_HOME/kylo-ui/bin/run-kylo-ui-with-debug.sh