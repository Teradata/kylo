#!/bin/bash

echo "Removing custom JAVA_HOME from kylo-ui and kylo-services"
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/kylo/kylo-services/bin/run-kylo-services.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/kylo/kylo-services/bin/run-kylo-services.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/kylo/kylo-services/bin/run-kylo-services-with-debug.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/kylo/kylo-services/bin/run-kylo-services-with-debug.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/kylo/kylo-ui/bin/run-kylo-ui.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/kylo/kylo-ui/bin/run-kylo-ui.sh
sed -i '/export JAVA_HOME=\/opt\/java\/current/d' /opt/kylo/kylo-ui/bin/run-kylo-ui-with-debug.sh
sed -i '/export PATH=\$JAVA_HOME\/bin\:\$PATH/d' /opt/kylo/kylo-ui/bin/run-kylo-ui-with-debug.sh