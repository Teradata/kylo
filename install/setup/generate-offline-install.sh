#!/bin/bash

NIFI_VERSION=1.3.0

cd /opt/kylo/setup

wget https://archive.apache.org/dist/activemq/5.15.0/apache-activemq-5.15.0-bin.tar.gz -P ./activemq

# Modify to DEB file if necessary
# wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.5.0.deb -P ./elasticsearch/
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.5.0.rpm -P ./elasticsearch/

wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz -P ./java

wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip -P ./java

wget https://archive.apache.org/dist/nifi/${NIFI_VERSION}/nifi-${NIFI_VERSION}-bin.tar.gz -P ./nifi

cp /opt/kylo/kylo-services/lib/mariadb-java-client-*.jar ./nifi

tar -cvf kylo-install.tar *
