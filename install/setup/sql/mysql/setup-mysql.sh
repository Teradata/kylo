#!/bin/bash

MYSQL_DIR=$(dirname $0)
echo "Executing the master script to install all database scripts"

$MYSQL_DIR/thinkbig/0.2.0/setup-mysql.sh $1 $2
$MYSQL_DIR/thinkbig/0.3.0/update.sh $1 $2
$MYSQL_DIR/thinkbig/0.3.1/update.sh $1 $2

mysql -u$1 --password=$2 -e 'show databases;'

echo "Database installation complete"