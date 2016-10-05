#!/usr/bin/env bash
mysql -h $1 -u $2 --password=$3  -e "CALL thinkbig.delete_feed('$3','$4');"