#!/usr/bin/env bash

MY_DIR=$(dirname $0)

mysql -h $1 -u $2 --password=$3 < $MY_DIR/delete_feed_jobs.sql
mysql -h $1 -u $2 --password=$3 < $MY_DIR/delete_feed_metadata.sql
mysql -h $1 -u $2 --password=$3 < $MY_DIR/delete_feed.sql