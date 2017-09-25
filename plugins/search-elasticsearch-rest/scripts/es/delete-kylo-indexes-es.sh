#!/bin/bash

#############################################################
# Use this script to delete Kylo indexes from Elasticsearch #
#############################################################

ELASTIC_SEARCH_HOST=$1
ELASTIC_SEARCH_REST_PORT=$2

if [ $# -eq 2 ]
then
    echo "Deleting kylo indexes in Elasticsearch: host="$ELASTIC_SEARCH_HOST", port="$ELASTIC_SEARCH_REST_PORT
else
    echo "Usage: <command> <host> <rest-port>"
    echo "Examples values:"
    echo " host: localhost"
    echo " rest-port: 9200"
    exit 1
fi

kylo_indexes=kylo-data,kylo-datasources,kylo-categories-metadata,kylo-categories-default,kylo-feeds-metadata,kylo-feeds-default

for KYLO_INDEX in $(echo $kylo_indexes | sed "s/,/ /g")
do
    curl -XDELETE $ELASTIC_SEARCH_HOST:$ELASTIC_SEARCH_REST_PORT/$KYLO_INDEX?pretty
done

echo "Done"
