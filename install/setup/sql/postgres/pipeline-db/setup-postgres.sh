#!/bin/bash

if [[ $# -gt 0 ]] ; then
    echo "Usage is: sudo ./setup-postgres.sh"
    exit 1
fi

## CHANGE below to password for postgres user

sudo -u postgres psql -tAc "create user pguser with password 'thinkbig';";
sudo -u postgres psql -f setup-postgres-database.sql

echo "Created pipeline_db database";
sudo -u postgres psql -f schema-postgres.sql
sudo -u postgres psql -f setup-postgres-tables.sql
echo "Created Spring Batch tables";
sudo -u postgres psql -f setup-postgres-indexes.sql
sudo -u postgres psql -tAc "ALTER DATABASE pipeline_db owner TO pguser;"
sudo -u postgres psql -d pipeline_db -tAc "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA PUBLIC TO pguser;"
sudo -u postgres psql -d pipeline_db -tAc "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA PUBLIC TO pguser;"
echo "Created Indexes";
