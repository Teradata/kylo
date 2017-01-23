#!/usr/bin/env bash
mysql -h $1 -u $2 --password=$3  -e "CALL kylo.delete_feed('$3','$4');"