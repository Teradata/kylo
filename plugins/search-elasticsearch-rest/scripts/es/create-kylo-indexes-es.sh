#!/bin/bash

#################################################################################################
#	Run this script before starting Kylo services for the 1st time, for Elasticsearch (v2,v5)   #
#################################################################################################

ELASTIC_SEARCH_HOST=$1
ELASTIC_SEARCH_REST_PORT=$2
NUMBER_OF_SHARDS=$3
NUMBER_OF_REPLICAS=$4

if [ $# -eq 4 ]
then
	echo "Setting up kylo indexes in Elasticsearch: host="$ELASTIC_SEARCH_HOST", port="$ELASTIC_SEARCH_REST_PORT", num-shards="$NUMBER_OF_SHARDS", num-replicas="$NUMBER_OF_REPLICAS
else
	echo "Usage: <command> <host> <rest-port> <num-shards> <num-replicas>"
	echo "Examples values:"
	echo " host: localhost"
	echo " rest-port: 9200"
	echo " num-shards: 1"
	echo " num-replicas: 1"
	echo " Note: num-shards and num-replicas can be set to 1 for development environment" 
	exit 1
fi

kylo_indexes=kylo-data,kylo-datasources,kylo-categories-metadata,kylo-categories-default,kylo-feeds-metadata,kylo-feeds-default

BODY="{
		\"settings\" : {
			\"index\" : {
				\"number_of_shards\" : "$NUMBER_OF_SHARDS", 
				\"number_of_replicas\" : "$NUMBER_OF_REPLICAS"
				}
		}
	}"
	
for KYLO_INDEX in $(echo $kylo_indexes | sed "s/,/ /g")
do
	curl -XPUT $ELASTIC_SEARCH_HOST:$ELASTIC_SEARCH_REST_PORT/$KYLO_INDEX?pretty -H 'Content-Type: application/json' -d"$BODY"
done

echo "Done"
