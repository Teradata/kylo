#!/bin/bash

MY_DIR=$(dirname $0)
mysql -u$1 --password=$2 < $MY_DIR/schema-0.4.0-upgrade.sql
echo "Updated to 0.4.0 release";
