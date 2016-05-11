#!/bin/bash

if [[ $# -ne 1 ]] ; then
    echo "Usage is: drop-mysql.sh [DROP]"
    echo " - for example: ./drop-mysql.sh DROP"
    exit 1
fi

if [[ "$1" == "DROP" ]]
then
    echo "* Dropping thinkbig database..."
    mysql -u root  -e 'drop database thinkbig;'
fi



 
