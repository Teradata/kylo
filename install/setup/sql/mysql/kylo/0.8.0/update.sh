#!/bin/bash

MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/initial-liquibase-changelog.sql
echo "Updated to 0.8.0 release";
echo "From now on, continue with Liquibase please";

