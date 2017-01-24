#!/usr/bin/env bash

# This script will rename Kylo's v0.6.0 old "thinkbig" MySql schema to new "kylo" schema
# Takes 3 parameters: host, username and password, e.g.
# > ./rename-schema.sh localhost root secret

MY_DIR=$(dirname $0)

mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/../kylo/0.2.0/create-database.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/rename-tables.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/../kylo/0.4.0/delete-feed-jobs.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/../kylo/0.5.0/create_db_views.sql
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/../kylo/0.6.0/abandon-feed-jobs.sql
