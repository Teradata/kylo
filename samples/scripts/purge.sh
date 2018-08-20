#!/bin/bash

#
# Copyright (C) 2017 ThinkBig Analytics
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Please read before use:
# This example script purges old and unused partitions created by Kylo for ingest processing. Kylo
# uses multiple tables for data ingestion by convention. This data can usually be safely deleted after a short
# period of time. We suggest deleting VALID and FEED table partitions within 7-14 days and possibly holding
# PROFILE and INVALID for several months (or permanently) if space is not a concern.
#
# The logic in this script assumes Kylo's standard ingest conventions and use of HDFS-backed storage.
# Do not simply assume it is safe to execute in your environment!
#
# This script is inefficient. It uses Hive to extract table metadata. It is however metastore database independent
# (at the expense of performance). One might write an alternative version that queries the metastore
# directly to identify partitions and hdfs locations to delete.
#
# The script generates SQL and HDFS purge statements to files that are then executed at the end.  The execution is
# commented out by default so the scripts can be examined before use. Generated scripts
# can be run manually or automatically by uncommenting the last several lines.
#
# Again, carefully evaluate impact of running this script prior to use.
#

# Exit and print error
die () {
    echo >&2 "$@"
    exit 1
}

# Validate parameters
[ "$#" -eq 2 ] || die "Usage: purge <#archiveDays> <tableType>"

if [[ !("$1" =~ ^[0-9]+$) ]]; then
   die "Expecting number for archiveDays, received $1"
fi

if [[ !("$2" =~ ^(VALID|PROFILE|INVALID|FEED)$) ]]; then
    die "Expecting valid table suffix in VALID,PROFILE,INVALID, or FEED but $2 provided"
fi

days=$1
tableType=$2
dttm=( $(($(date +%s%N)/1000000-($days*24*3600*1000))))

# reset script file
sql_script="$tableType.sql"
hdfs_script="$tableType.sh"
> $sql_script
> $hdfs_script

echo "Generating scripts $sql_script and $hdfs_script to delete all partitions and data older than $dttm for $tableType tables."

# Query all schemas and tables
schemas=( $(hive -S -e "show schemas"))
for schema in "${schemas[@]}"
do
  echo "Scanning schema $schema for eligible tables..."
  tables=( $(hive -S -e "show tables in $schema like '*_$tableType'"))

  for table in "${tables[@]}"
  do
     echo "    Analyzing partitions for table $table"
     ddl=""
     hdfs=""
     partitions=( $(hive -S -e "show partitions $schema.$table;"))
     for partition in "${partitions[@]}"
     do
        prefix=( $(echo "$partition"| cut -d'=' -f 1) )
        if [ "$prefix" = "processing_dttm" ]
        then
            dtpart=( $(echo "$partition"| cut -d'=' -f 2) )
            if (( dtpart < $dttm ))
            then
                # Feed tables are external so we need to drop partition and separately delete the data in HDFS
		if [ "$tableType" = "FEED" ]
		then
		    # Extract physical location of $partition"
                    location=( $(hive -S -e "describe formatted $schema.$table partition ($partition)" | grep 'Location: ' | cut -c 22-1000 ))
                    #echo "Partition is located at $location"
                    if [[ $location = *"$dtpart"* ]]
                    then
                       echo "        HDFS location $location will be deleted."
                       hdfs+="hdfs dfs -rm -r $location\n"
                    else
                       echo "        WARNING: $location does not contain expected name $dtpart so will be skipped."
                    fi
	        fi
                echo "        Partition $partition will be dropped."
                ddl+="ALTER TABLE $schema.$table DROP IF EXISTS PARTITION ($partition);\n";
            fi
        else
            echo "Skipping $partition due to unexpected partion structure"
        fi
     done
     echo -e $ddl >> $sql_script
     echo -e $hdfs >> $hdfs_script
  done
done
echo "Running script $sql_script and $hdfs_script"
# Uncomment below to automatically execute purge scripts
# hive -f $sql_script
# ./$hdfs_script