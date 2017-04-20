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
    cd $working_dir/elasticsearch

    if [ "$linux_type" == "chkonfig" ]; then
        echo "Executing RPM"
        rpm -ivh elasticsearch-2.3.0.rpm
    elif [ "$linux_type" == "update-rc.d" ]; then
        echo "Executing DEB"
        rpm -ivh elasticsearch-2.3.0.deb
    fi
    cp $working_dir/elasticsearch/elasticsearch.yml /etc/elasticsearch/
else
    cd /opt

    if [ "$linux_type" == "chkonfig" ]; then
        echo "Downloading RPM"
        curl -O https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/rpm/elasticsearch/2.3.0/elasticsearch-2.3.0.rpm
        echo "Executing RPM"
        rpm -ivh elasticsearch-2.3.0.rpm
        rm elasticsearch-2.3.0.rpm

        echo "Setup elasticsearch as a service"
        sudo chkconfig --add elasticsearch

    elif [ "$linux_type" == "update-rc.d" ]; then
        echo "Downloading DEB"
        curl -O https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/deb/elasticsearch/2.3.0/elasticsearch-2.3.0.deb
        echo "Executing DEB"
        dpkg -i elasticsearch-2.3.0.deb
        rm elasticsearch-2.3.0.deb

        echo "Setup elasticsearch as a service"
        update-rc.d elasticsearch defaults 95 10
    fi

    cp /opt/kylo/setup/elasticsearch/elasticsearch.yml /etc/elasticsearch/
    echo "Installing HQ plugin"
    /usr/share/elasticsearch/bin/plugin install royrusso/elasticsearch-HQ
fi

echo "Starting Elasticsearch"
sudo service elasticsearch start

echo "Elasticsearch install complete"