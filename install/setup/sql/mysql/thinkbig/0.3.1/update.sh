#!/bin/bash

MY_DIR=$(dirname $0)
mysql -u$1 --password=$2 < $MY_DIR/delete_feed_jobs.sql
echo "Updated to 0.3.1 release";
