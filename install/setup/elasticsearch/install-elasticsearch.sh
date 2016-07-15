#!/bin/bash
echo "Installing Elasticsearch"
offline=false
working_dir=$2

if [ $# > 1 ]
then
    if [ "$1" = "-o" ] || [ "$1" = "-O" ]
    then
        echo "Working in offline mode"
        offline=true
    fi
fi

if [ $offline = true ]
then
    cd $working_dir/elasticsearch
    echo "Executing RPM"
    rpm -ivh elasticsearch-2.3.0.rpm
    cp $working_dir/elasticsearch/elasticsearch.yml /etc/elasticsearch/
else
    cd /opt
    echo "Downloading RPM"
    curl -O https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/rpm/elasticsearch/2.3.0/elasticsearch-2.3.0.rpm
    echo "Executing RPM"
    rpm -ivh elasticsearch-2.3.0.rpm
    cp /opt/thinkbig/setup/elasticsearch/elasticsearch.yml /etc/elasticsearch/
    echo "Installing HQ plugin"
    /usr/share/elasticsearch/bin/plugin install royrusso/elasticsearch-HQ
fi

echo "Setup elasticsearch as a service"
sudo chkconfig --add elasticsearch

echo "Starting Elasticsearch"
sudo service elasticsearch start
rm elasticsearch-2.3.0.rpm
echo "Elasticsearch install complete"