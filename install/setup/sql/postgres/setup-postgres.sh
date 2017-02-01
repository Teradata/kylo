#!/bin/bash

if [[ $# -gt 0 ]] ; then
    echo "Usage is: sudo ./setup-postgres.sh"
    exit 1
fi

## CHANGE below to password for postgres user

sudo -u postgres psql -tAc "create user pguser with password 'kylo';";
sudo -u postgres psql -f kylo/create-database.sql

echo "Created kylo database";
sudo -u postgres psql -f kylo/schema-batch-postgres.sql
sudo -u postgres psql -f kylo/schema-batch-kylo-postgres.sql
echo "Created Operation Manager tables";
sudo -u postgres psql -f kylo/schema-batch-kylo-indexes.sql
sudo -u postgres psql -tAc "ALTER DATABASE kylo owner TO pguser;"
sudo -u postgres psql -d kylo -tAc "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA PUBLIC TO pguser;"
sudo -u postgres psql -d kylo -tAc "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA PUBLIC TO pguser;"
echo "Created Operation Manager Indexes";
sudo -u postgres psql -f kylo/schema-metadata-postgres.sql
echo 'Created Metadata Tables'
