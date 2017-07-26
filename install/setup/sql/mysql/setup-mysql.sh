#!/bin/bash

MYSQL_DIR=$(dirname $0)
echo "Executing the master script to install all database scripts"

$MYSQL_DIR/kylo/0.2.0/setup-mysql.sh $1 $2 $3
$MYSQL_DIR/kylo/0.3.0/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.3.1/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.4.0/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.4.2/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.5.0/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.6.0/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.7.0/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.7.1/update.sh $1 $2 $3
$MYSQL_DIR/kylo/0.8.0/update.sh $1 $2 $3

mysql -h $1 -u$2 --password=$3 -e 'show databases;'

echo "Database installation complete"