#!/bin/bash

if [[ $# -gt 0 ]] ; then
    echo "Usage is: sudo ./setup-postgres.sh"
    exit 1
fi

sudo -u postgres psql -f alter_tables.sql
echo "Updated to 0.3.0 release";