#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/schema-0.4.2-upgrade.sql
echo "Updated to 0.4.2 release";
