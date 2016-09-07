#!/bin/bash
#Note: edit /etc/default/activemq to change Java memory parameters

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

echo "Create the /opt/activemq directory"
mkdir /opt/activemq
cd /opt/activemq

if [ $offline = true ]
then
    cp $working_dir/activemq/apache-activemq-5.13.3-bin.tar.gz .
else
    echo "Download activemq and install"
    curl -O http://ftp.wayne.edu/apache//activemq/5.13.3/apache-activemq-5.13.3-bin.tar.gz
fi

if ! [ -f apache-activemq-5.13.3-bin.tar.gz ]
then
    echo "Installation file not found.. exiting"
    exit 1
fi

tar -xvf apache-activemq-5.13.3-bin.tar.gz
rm -f apache-activemq-5.13.3-bin.tar.gz
ln -s apache-activemq-5.13.3 current

echo "Installing as a service"
# http://activemq.apache.org/unix-shell-script.html

chown -R activemq:activemq /opt/activemq
cp /opt/activemq/current/bin/env /etc/default/activemq
sed -i '~s/^ACTIVEMQ_USER=""/ACTIVEMQ_USER="activemq"/' /etc/default/activemq
chmod 644 /etc/default/activemq
ln -snf  /opt/activemq/current/bin/activemq /etc/init.d/activemq
chkconfig --add activemq
chkconfig activemq on
service activemq start
