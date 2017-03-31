#!/usr/bin/env bash

TARGET=kylo-db-update-script.sql
PROPS=/opt/kylo/kylo-services/conf/application.properties

echo "Reading configuration properties from ${PROPS}"

USERNAME=`grep "^spring.datasource.username=" ${PROPS} | cut -d'=' -f2`
PASSWORD=`grep "^spring.datasource.password=" ${PROPS} | cut -d'=' -f2`
DRIVER=`grep "^spring.datasource.driverClassName=" ${PROPS} | cut -d'=' -f2`
URL=`grep "^spring.datasource.url=" ${PROPS} | cut -d'=' -f2`

CP='/opt/kylo/kylo-services/lib/liquibase-core-3.5.3.jar.jar:/opt/kylo/kylo-services/lib/*'
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
