#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "[Correct usage] Three parameters are needed: host, user, password"
    exit -1
fi
MYSQL_DIR=$(dirname $0)

read -p "WARNING: This will drop thinkbig schema. Do you want to proceed (y/n)? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    $MYSQL_DIR/kylo/migration/drop_thinkbig_schema.sh $1 $2 $3
    echo "Done"
    exit
fi
echo "Aborted"