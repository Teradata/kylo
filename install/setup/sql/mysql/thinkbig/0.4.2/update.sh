#!/bin/bash

MY_DIR=$(dirname $0)
mysql -u$2 --password=$3 --database=thinkbig -B -N -e "SHOW TABLES" | awk '{print "SET foreign_key_checks = 0; ALTER TABLE", $1, "CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci; SET foreign_key_checks = 1; "}' | mysql --database=thinkbig
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/schema-0.4.2-upgrade.sql
echo "Updated to 0.4.2 release";

