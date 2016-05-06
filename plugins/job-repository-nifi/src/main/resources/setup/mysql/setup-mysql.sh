#!/bin/bash

echo "Installing thinkbig nifi plugin"
mysql -u root -e 'CREATE DATABASE thinkbig_nifi;'
mysql -u root thinkbig_nifi < mysql-schema.sql
echo "Installation complete"