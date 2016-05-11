#!/bin/bash

if [[ $# -ne 1 ]] ; then
    echo "Usage is: drop-mysql.sh [DROP]"
    echo " - for example: ./drop-mysql.sh DROP"
    exit 1
fi

if [[ "$1" == "DROP" ]]
then
    echo "* Dropping pipeline_db database..."
    mysql -u root  -e 'drop database pipeline_db;'
    echo "* Dropping thinkbig_nifi database..."
    mysql -u root  -e 'drop database thinkbig_nifi;'
    echo "* Dropping metadata database..."
    mysql -u root  -e 'drop database metadata;'
fi



 
