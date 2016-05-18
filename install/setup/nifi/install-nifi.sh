#!/bin/bash
NIFI_DATA=/opt/nifi/data

echo "Installing NiFI"
echo "Creating a new nifi user"
useradd -m nifi -d /opt/nifi
cd /opt/nifi
echo "Download nifi distro and install"
wget http://apache.mesi.com.ar/nifi/0.6.1/nifi-0.6.1-bin.tar.gz
tar -xvf nifi-0.6.1-bin.tar.gz
ln -s nifi-0.6.1 current

echo "Externalizing NiFi data files and folders to support upgrades"
mkdir -p $NIFI_DATA/conf
mv /opt/nifi/current/conf/authority-providers.xml $NIFI_DATA/conf
mv /opt/nifi/current/conf/login-identity-providers.xml $NIFI_DATA/conf


echo "Changing permissions to the nifi user"
chown -R nifi:users /opt/nifi
echo "NiFi installation complete"
