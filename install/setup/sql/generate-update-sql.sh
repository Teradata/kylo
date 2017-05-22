#!/usr/bin/env bash

KYLO_HOME=/opt/kylo

if [ $# -eq 1 ]
then
    echo "Setting the KYLO_HOME to $1"
    KYLO_HOME=$1
fi

TARGET=kylo-db-update-script.sql
PROPS=$KYLO_HOME/kylo-services/conf/application.properties

echo "Reading configuration properties from ${PROPS}"

USERNAME=`grep "^spring.datasource.username=" ${PROPS} | cut -d'=' -f2`
PASSWORD=`grep "^spring.datasource.password=" ${PROPS} | cut -d'=' -f2`
DRIVER=`grep "^spring.datasource.driverClassName=" ${PROPS} | cut -d'=' -f2`
URL=`grep "^spring.datasource.url=" ${PROPS} | cut -d'=' -f2`

CP="$KYLO_HOME/kylo-services/lib/liquibase-core-3.5.3.jar.jar:$KYLO_HOME/kylo-services/lib/*"
echo "Loading classpath: ${CP}"

echo "Generating ${TARGET} for ${URL}, connecting as ${USERNAME}"

java -cp ${CP} \
    liquibase.integration.commandline.Main \
     --changeLogFile=com/thinkbiganalytics/db/master.xml \
     --driver=${DRIVER} \
     --url=${URL} \
     --username=${USERNAME} \
     --password=${PASSWORD} \
     updateSQL > ${TARGET}

echo "Replacing delimiter placeholders"
sed -i.bac "s/-- delimiter placeholder //g" ${TARGET}
