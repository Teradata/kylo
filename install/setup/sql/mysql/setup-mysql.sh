#!/bin/bash

mysql -u $1 --password=$2 < /opt/thinkbig/setup/sql/mysql/pipeline-db/create-database.sql
echo "Created pipeline_db database";
mysql -u $1 --password=$2 pipeline_db < /opt/thinkbig/setup/sql/mysql/pipeline-db/schema-mysql.sql
mysql -u $1 --password=$2 pipeline_db < /opt/thinkbig/setup/sql/mysql/pipeline-db/setup-mysql-tables.sql
echo "Created Spring Batch Tables";
mysql -u $1 --password=$2 pipeline_db < /opt/thinkbig/setup/sql/mysql/pipeline-db/setup-mysql-indexes.sql
echo "Created Indexes";

mysql -u $1 --password=$2 < /opt/thinkbig/setup/sql/mysql/metadata/schema-mysql.sql
#mysql -u root metadata < /opt/thinkbig/setup/sql/mysql/metadata/constraints-mysql.sql
echo 'Created metadata database'

mysql -u $1 --password=$2 < /opt/thinkbig/setup/sql/mysql/thinkbig-nifi/mysql-schema.sql
echo 'Created thinkbig_nifi database'

mysql -u $1 --password=$2 -e 'show databases;'