#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/remove_old_schema.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/schema-0.4.0-upgrade.sql
mysql -h $1 -u$2 --password=$3 < ${MY_DIR}/delete-feed-jobs.sql
echo "Updated to 0.4.0 release";
