#!/usr/bin/env bash
mysql -h $1 -u $2 --password=$3 < /opt/thinkbig/setup/sql/mysql/kylo/stored_procedures/delete_feed_jobs.sql
mysql -h $1 -u $2 --password=$3 < /opt/thinkbig/setup/sql/mysql/kylo/stored_procedures/delete_feed_metadata.sql
mysql -h $1 -u $2 --password=$3 < /opt/thinkbig/setup/sql/mysql/kylo/stored_procedures/delete_feed.sql