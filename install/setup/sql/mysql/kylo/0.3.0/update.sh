#!/bin/bash

MY_DIR=$(dirname $0)
mysql -h $1 -u$2 --password=$3 < $MY_DIR/alter_tables.sql
echo "Updated to 0.3.0 release";
