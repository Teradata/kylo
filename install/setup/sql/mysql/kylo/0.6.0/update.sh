#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/schema-0.6.0-upgrade.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/abandon-feed-jobs.sql
echo "Updated to 0.6.0 release";

