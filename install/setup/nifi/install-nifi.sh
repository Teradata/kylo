#!/bin/bash
NIFI_DATA=/opt/nifi/data
NIFI_VERSION=1.0.0

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

echo "Installing NiFI"
echo "Creating a new nifi user"
mkdir /opt/nifi
cd /opt/nifi

if [ $offline = true ]
then
    cp $working_dir/nifi/nifi-${NIFI_VERSION}-bin.tar.gz .
else
    echo "Download nifi distro and install"
    curl -O https://archive.apache.org/dist/nifi/${NIFI_VERSION}/nifi-${NIFI_VERSION}-bin.tar.gz
fi

if ! [ -f nifi-${NIFI_VERSION}-bin.tar.gz ]
then
    echo "Working in online mode and file not found.. exiting"
    exit 1
fi

tar -xvf nifi-${NIFI_VERSION}-bin.tar.gz
rm -f current
ln -s nifi-${NIFI_VERSION} current

echo "Externalizing NiFi data files and folders to support upgrades"
mkdir -p $NIFI_DATA/conf
mv /opt/nifi/current/conf/authorizers.xml $NIFI_DATA/conf
mv /opt/nifi/current/conf/login-identity-providers.xml $NIFI_DATA/conf


echo "Changing permissions to the nifi user"
chown -R nifi:nifi /opt/nifi
echo "NiFi installation complete"
