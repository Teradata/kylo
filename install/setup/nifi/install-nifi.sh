#!/bin/bash
# make project directory
mkdir /opt/nifi
cd /opt/nifi
# download nifi and install
wget https://archive.apache.org/dist/nifi/0.5.1/nifi-0.5.1-bin.tar.gz
tar -xvf nifi-0.5.1-bin.tar.gz
ln -s nifi-0.5.1 current
