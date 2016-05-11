#!/bin/bash

if [[ $# -gt 0 ]] ; then
    echo "Usage is: sudo ./setup-postgres.sh"
    exit 1
fi

## CHANGE below to password for postgres user

sudo -u postgres psql -tAc "create user pguser with password 'thinkbig';";
sudo -u postgres psql -f thinkbig/create-database.sql

echo "Created thinkbig database";
sudo -u postgres psql -f thinkbig/schema-batch-postgres.sql
sudo -u postgres psql -f thinkbig/schema-batch-thinkbig-postgres.sql
echo "Created Operation Manager tables";
sudo -u postgres psql -f thinkbig/schema-batch-thinkbig-indexes.sql
sudo -u postgres psql -tAc "ALTER DATABASE thinkbig owner TO pguser;"
sudo -u postgres psql -d thinkbig -tAc "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA PUBLIC TO pguser;"
sudo -u postgres psql -d thinkbig -tAc "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA PUBLIC TO pguser;"
echo "Created Operation Manager Indexes";
sudo -u postgres psql -f thinkbig/schema-metadata-postgres.sql
echo 'Created Metadata Tables'
