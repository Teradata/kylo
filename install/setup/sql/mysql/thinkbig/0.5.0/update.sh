#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/schema-0.5.0-upgrade.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/check_data_job_relationships.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/alter_datetime_columns.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/create_db_views.sql
echo "Updated to 0.5.0 release";

