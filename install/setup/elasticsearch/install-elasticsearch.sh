#!/bin/bash
echo "Installing Elasticsearch"
cd /opt
echo "Downloading RPM"
wget https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/rpm/elasticsearch/2.3.0/elasticsearch-2.3.0.rpm
echo "Executing RPM"
rpm -ivh elasticsearch-2.3.0.rpm
cp ./elasticsearch.yml /etc/elasticsearch/
echo "Setup elasticsearch as a service"
sudo chkconfig --add elasticsearch
echo "Installing HQ plugin"
/usr/share/elasticsearch/bin/plugin install royrusso/elasticsearch-HQ
echo "Starting Elasticsearch"
sudo service elasticsearch start
rm elasticsearch-2.3.0.rpm
echo "Elasticsearch install complete"