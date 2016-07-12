#!/bin/bash

mysql -u$1 --password=$2 < /opt/thinkbig/setup/sql/mysql/thinkbig/create-database.sql
echo "Created thinkbig database";
mysql -u$1 --password=$2 thinkbig < /opt/thinkbig/setup/sql/mysql/thinkbig/schema-batch-mysql.sql
mysql -u$1 --password=$2 thinkbig < /opt/thinkbig/setup/sql/mysql/thinkbig/schema-batch-thinkbig-mysql.sql
echo "Created Operation Manager Tables";
mysql -u$1 --password=$2 thinkbig < /opt/thinkbig/setup/sql/mysql/thinkbig/schema-batch-thinkbig-indexes.sql
echo "Created Operation Manager Indexes";

mysql -u$1 --password=$2 thinkbig < /opt/thinkbig/setup/sql/mysql/thinkbig/schema-metadata-mysql.sql
mysql -u$1 --password=$2 thinkbig < /opt/thinkbig/setup/sql/mysql/thinkbig/schema-metadata-constraints.sql
echo 'Created Metadata Tables'
mysql -u$1 --password=$2 thinkbig < /opt/thinkbig/setup/sql/mysql/thinkbig/schema-metadata-grants.sql
echo 'Granted SQL for user nifi'

mysql -u$1 --password=$2 -e 'show databases;'