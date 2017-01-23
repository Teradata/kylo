#!/bin/bash

MY_DIR=$(dirname $0)
mysql -h$1 -u$2 --password=$3 --database=kylo -B -N -e "SHOW TABLES" | awk '{print "SET foreign_key_checks = 0; ALTER TABLE", $1, "CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci; SET foreign_key_checks = 1; "}' | mysql --database=kylo -h$1 -u$2 --password=$3
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/schema-0.4.2-upgrade.sql
echo "Updated to 0.4.2 release";

