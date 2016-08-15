#!/bin/bash

MY_DIR=$(dirname $0)
mysql -u$1 --password=$2 < $MY_DIR/create-database.sql
echo "Created thinkbig database";
mysql -u$1 --password=$2 thinkbig < $MY_DIR/schema-batch-mysql.sql
mysql -u$1 --password=$2 thinkbig < $MY_DIR/schema-batch-thinkbig-mysql.sql
echo "Created Operation Manager Tables";
mysql -u$1 --password=$2 thinkbig < $MY_DIR/schema-batch-thinkbig-indexes.sql
echo "Created Operation Manager Indexes";
mysql -u$1 --password=$2 thinkbig < $MY_DIR/stored_procedures/delete_feed_jobs.sql
echo "Created stored procedures"

mysql -u$1 --password=$2 thinkbig < $MY_DIR/schema-metadata-mysql.sql
mysql -u$1 --password=$2 thinkbig < $MY_DIR/schema-metadata-constraints.sql
echo 'Created Metadata Tables'
mysql -u$1 --password=$2 thinkbig < $MY_DIR/schema-metadata-grants.sql
echo 'Granted SQL for user nifi'

echo "0.2.0 scripts installed"
