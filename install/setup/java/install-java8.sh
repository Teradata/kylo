#!/bin/bash

cd /opt
mkdir java
cd java
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u92-b14/jdk-8u92-linux-x64.tar.gz
tar -xvf jdk-8u92-linux-x64.tar.gz
rm -f jdk-8u92-linux-x64.tar.gz
ln -s jdk1.8.0_92 current