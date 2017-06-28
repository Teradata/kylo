#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/alter_tables.sql
echo "Updated to 0.7.1 release";

