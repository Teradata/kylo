#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/add_table_indexes_and_foreign_keys.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/remove_unused_tables.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/delete-feed-jobs.sql
echo "Updated to 0.7.0 release";

