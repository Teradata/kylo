#!/bin/bash

MY_DIR=$(dirname $0)
mysql -u$1 --password=$2 < $MY_DIR/alter_tables.sql
mysql -u$1 --password=$2 < $MY_DIR/../0.2.0/stored_procedures/delete_feed_jobs.sql
echo "Updated to 0.3.0 release";
