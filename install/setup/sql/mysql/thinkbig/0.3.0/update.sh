#!/bin/bash

mysql -u$1 --password=$2 < /opt/thinkbig/setup/sql/mysql/0.3.0/alter_tables.sql
echo "Updated to 0.3.0 release";