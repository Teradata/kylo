mysql -u $1 --password=$2 < /opt/thinkbig/setup/sql/mysql/thinkbig/stored_procedures/delete_feed_jobs.sql
mysql -u $1 --password=$2 < /opt/thinkbig/setup/sql/mysql/thinkbig/stored_procedures/delete_feed_metadata.sql
mysql -u $1 --password=$2 < /opt/thinkbig/setup/sql/mysql/thinkbig/stored_procedures/delete_feed.sql