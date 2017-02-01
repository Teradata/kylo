#!/bin/bash

cd /opt/kylo/setup

wget https://archive.apache.org/dist/activemq/5.13.3/apache-activemq-5.13.3-bin.tar.gz -P ./activemq

wget https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/rpm/elasticsearch/2.3.0/elasticsearch-2.3.0.rpm -P ./elasticsearch/

wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u92-b14/jdk-8u92-linux-x64.tar.gz -P ./java

wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip -P ./java

wget https://archive.apache.org/dist/nifi/1.0.0/nifi-1.0.0-bin.tar.gz -P ./nifi

cp /opt/kylo/kylo-services/lib/mysql-connector-java-*.jar ./nifi

tar -cvf kylo-install.tar *
