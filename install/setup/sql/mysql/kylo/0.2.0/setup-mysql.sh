#!/bin/bash

MY_DIR=$(dirname $0)
mysql -h $1 -u$2 --password=$3 < $MY_DIR/create-database.sql
echo "Created kylo database";
mysql -h $1 -u$2 --password=$3 kylo < $MY_DIR/schema-batch-mysql.sql
mysql -h $1 -u$2 --password=$3 kylo < $MY_DIR/schema-batch-kylo-mysql.sql
echo "Created Operation Manager Tables";
mysql -h $1 -u$2 --password=$3 kylo < $MY_DIR/schema-batch-kylo-indexes.sql
echo "Created Operation Manager Indexes";

#mysql -h $1 -u$2 --password=$3 kylo < $MY_DIR/schema-metadata-mysql.sql
#mysql -h $1 -u$2 --password=$3 kylo < $MY_DIR/schema-metadata-constraints.sql
echo 'Created Metadata Tables'
#mysql -h $1 -u$2 --password=$3 kylo < $MY_DIR/schema-metadata-grants.sql
echo 'Granted SQL for user nifi'

echo "0.2.0 scripts installed"
