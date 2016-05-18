#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
    exit 1
fi
echo "Dropping hive database $1"
hive -e "drop database $1 cascade;"
echo "Dropping HDFS folders for $1"
hdfs dfs -rm -r /model.db/$1
hdfs dfs -rm -r /etl/$1
hdfs dfs -rm -r /app/warehouse/$1
hdfs dfs -rm -r /archive/$1