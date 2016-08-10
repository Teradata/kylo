#!/bin/bash

MY_DIR=$(dirname $0)
mysql -u$1 --password=$2 < $MY_DIR/alter_tables.sql
echo "Updated to 0.3.0 release";