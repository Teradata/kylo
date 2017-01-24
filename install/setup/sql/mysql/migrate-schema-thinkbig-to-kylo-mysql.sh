#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "[Correct usage] Three parameters are needed: host, user, password"
    exit -1
fi
MYSQL_DIR=$(dirname $0)
echo "*** Performing migration of schema from thinkbig to kylo via two step-process ***"
echo "*** Step 1: Setting up kylo schema ***"
$MYSQL_DIR/setup-mysql.sh $1 $2 $3
echo
echo "*** Step 2: Migrating thinkbig schema to kylo schema ***"
$MYSQL_DIR/kylo/migration/migrate_from_thinkbig_schema.sh $1 $2 $3
echo
echo "NOTE: After verifying kylo schema, thinkbig schema can be dropped."
echo "Done"