#!/usr/bin/env bash

java -cp '/opt/kylo/kylo-services/lib/liquibase-core-3.5.3.jar.jar:/opt/kylo/kylo-services/lib/*' \
    liquibase.integration.commandline.Main \
     --driver=org.mariadb.jdbc.Driver \
     --changeLogFile=com/thinkbiganalytics/db/master.xml \
     --url="jdbc:mysql://localhost:3306/kylo" \
     --username=root \
     --password=hadoop \
     updateSQL > kylo-db-update-script.sql
