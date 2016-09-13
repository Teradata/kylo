#!/bin/bash
#Note: edit /etc/default/activemq to change Java memory parameters

ACTIVEMQ_VERSION=5.13.4
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

echo "Create activemq user and create /opt/activemq directory"
useradd -m activemq -d /opt/activemq
cd /opt/activemq

if [ $offline = true ]
then
    cp $working_dir/activemq/apache-activemq-${ACTIVEMQ_VERSION}-bin.tar.gz .
else
    echo "Download activemq and install"
    curl -O http://ftp.wayne.edu/apache/activemq/${ACTIVEMQ_VERSION}/apache-activemq-${ACTIVEMQ_VERSION}-bin.zip
fi

if ! [ -f apache-activemq-${ACTIVEMQ_VERSION}-bin.zip ]
then
    echo "Installation file not found.. exiting"
    exit 1
fi

unzip apache-activemq-${ACTIVEMQ_VERSION}-bin.zip
rm -f apache-activemq-${ACTIVEMQ_VERSION}-bin.zip
ln -s apache-activemq-${ACTIVEMQ_VERSION} current

echo "Installing as a service"
# http://activemq.apache.org/unix-shell-script.html

chown -R activemq:users /opt/activemq
cp /opt/activemq/current/bin/env /etc/default/activemq
sed -i '~s/^ACTIVEMQ_USER=""/ACTIVEMQ_USER="activemq"/' /etc/default/activemq
chmod 644 /etc/default/activemq
ln -snf  /opt/activemq/current/bin/activemq /etc/init.d/activemq
chkconfig --add activemq
chkconfig activemq on
service activemq start
