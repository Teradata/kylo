#!/bin/bash
MY_DIR=$(dirname $0)
mysql -f -h $1 -u$2 --password=$3 < ${MY_DIR}/drop_thinkbig_schema.sql
echo "Dropped thinkbig schema"