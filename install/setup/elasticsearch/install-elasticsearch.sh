#!/bin/bash

#########
#  Example 1: ./install-elasticsearch /opt/kylo-dev/setup
#  Example 2: ./install-elasticsearch /tmp/offline-install -o
#########
echo "Installing Elasticsearch"
offline=false
SETUP_FOLDER=/opt/kylo/setup

if [ $# -eq 0 ]
then
    echo "No setup folder specified. Defaulting to /opt/kylo/setup"
elif [ $# -eq 1 ]
then
    echo "The setup folder is $1 "
    SETUP_FOLDER=$1
elif [ $# -eq 2 ]
then
    echo "The setup folder is $1 "
    SETUP_FOLDER=$1
    ES_JAVA_HOME=$2
elif [ $# -eq 3 ] && ([ "$3" = "-o" ] || [ "$3" = "-O" ])
then
    echo "Working in offline mode"
    offline=true
    SETUP_FOLDER=$1
    ES_JAVA_HOME=$2
else
    echo "Unknown arguments. The first argument should be the path to the setup folder. Optional you can pass a second argument to set offline mode. The value is -o or -O "
    exit 1
fi

# function for determining way to handle startup scripts
function get_linux_type {
    # redhat
    which chkconfig > /dev/null && echo "chkonfig" && return 0
    # ubuntu sysv
    which update-rc.d > /dev/null && echo "update-rc.d" && return 0
    echo "Couldn't recognize linux version, after installation you need to do these steps manually:"
    echo " * add proper header to /etc/init.d/{kylo-ui,kylo-services,kylo-spark-shell} files"
    echo " * set them to autostart"
}

linux_type=$(get_linux_type)

if [ $offline = true ]
then
    cd $SETUP_FOLDER/elasticsearch

    if [ "$linux_type" == "chkonfig" ]; then
        echo "Executing RPM"
        rpm -ivh elasticsearch-5.5.0.rpm
    elif [ "$linux_type" == "update-rc.d" ]; then
        echo "Executing DEB"
        dpkg -i elasticsearch-5.5.0.deb
    fi
else
    cd $SETUP_FOLDER/elasticsearch

    if [ "$linux_type" == "chkonfig" ]; then
        echo "Downloading RPM"
        curl -O -k https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.5.0.rpm
        echo "Executing RPM"
        rpm -ivh elasticsearch-5.5.0.rpm
        rm elasticsearch-5.5.0.rpm

        echo "Setup elasticsearch as a service"
        sudo chkconfig --add elasticsearch

    elif [ "$linux_type" == "update-rc.d" ]; then
        echo "Downloading DEB"
        curl -O -k https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.5.0.deb
        echo "Executing DEB"
        dpkg -i elasticsearch-5.5.0.deb
        rm elasticsearch-5.5.0.deb

        echo "Setup elasticsearch as a service"
        update-rc.d elasticsearch defaults 95 10
    fi

fi

sed -i "s|#cluster.name: my-application|cluster.name: demo-cluster|" /etc/elasticsearch/elasticsearch.yml
sed -i "s|#network.host: 192.168.0.1|network.host: localhost|" /etc/elasticsearch/elasticsearch.yml

if [ -z "$ES_JAVA_HOME" ] || ["$ES_JAVA_HOME" == "SYSTEM_JAVA" ]
then
 echo "No Java home has been specified for Elasticsearch. Using the system Java home"
else
  echo "JAVA_HOME=$ES_JAVA_HOME" >> /etc/sysconfig/elasticsearch
fi

echo "Starting Elasticsearch"
sudo service elasticsearch start

echo "Elasticsearch install complete"

SERVICE='elasticsearch'

function check_service {
        if service $SERVICE status | grep running > /dev/null
        then
                #echo "$SERVICE is running"
                retval=0
        else
                #echo "$SERVICE is NOT running"
                retval=1
        fi
}

retval=1
numtries=1
maxtries=10

echo "Waiting for $SERVICE service to start ..."
while [ "$retval" != 0 ]
do
        echo "."
        sleep 1s
        check_service
        ((numtries++))

        if [ "$numtries" -gt "$maxtries" ]
        then
                echo "Timeout reached"
                break
        fi
done

if [ "$retval" == 0 ]
then
        echo "Waiting for 10 seconds for the engine to start up, and then will create Kylo indexes in Elasticsearch."
        echo "NOTE: If they already exist, an index_already_exists_exception will be reported. This is OK."
        sleep 10s
        $SETUP_FOLDER/../bin/create-kylo-indexes-es.sh localhost 9200 1 1
else
        echo "$SERVICE service did not start within a reasonable time. Please check and start it. Then, execute this script manually before starting Kylo."
        echo "This script will create Kylo indexes in Elasticsearch."
        echo "NOTE: If they already exist, an index_already_exists_exception will be reported. This is OK."
        echo "$SETUP_FOLDER/../bin/create-kylo-indexes-es.sh localhost 9200 1 1"
fi

echo "Elasticsearch index creation complete"