#!/bin/bash

if [ $# -ne 1 ]
then
    echo "Wrong number of arguments. You must pass in the kylo home location"
    exit 1
fi

KYLO_HOME=$1

echo "Removing custom JAVA_HOME from kylo-ui and kylo-services"
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' $KYLO_HOME/kylo-services/bin/run-kylo-services.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' $KYLO_HOME/kylo-services/bin/run-kylo-services.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' $KYLO_HOME/kylo-services/bin/run-kylo-services-with-debug.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' $KYLO_HOME/kylo-services/bin/run-kylo-services-with-debug.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' $KYLO_HOME/kylo-ui/bin/run-kylo-ui.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' $KYLO_HOME/kylo-ui/bin/run-kylo-ui.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' $KYLO_HOME/kylo-ui/bin/run-kylo-ui-with-debug.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' $KYLO_HOME/kylo-ui/bin/run-kylo-ui-with-debug.sh