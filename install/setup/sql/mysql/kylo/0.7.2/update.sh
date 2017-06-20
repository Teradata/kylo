#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/streaming_table_view_updates.sql
echo "Updated to 0.7.2 release";

