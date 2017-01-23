#!/bin/bash

if [[ $# -ne 1 ]] ; then
    echo "Usage is: drop-postgres.sh [DROP]"
    echo " - for example: ./drop-postgres.sh DROP"
    exit 1
fi

echo "* Dropping kylo database..."
if [[ "$1" == "DROP" ]]
then
## CHANGE below to password for postgres user
###Execute the setup scripts as the postgres user
   sudo -u postgres psql -c 'drop database kylo;'
fi
