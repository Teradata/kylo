#!/bin/bash

OFFLINE=false
WORKING_DIR=$2
NIFI_INSTALL_HOME=/opt/nifi
KYLO_INSTALL_HOME=/opt/thinkbig

if [ $# > 1 ]
then
    if [ "$1" = "-o" ] || [ "$1" = "-O" ]
    then
        echo "Working in offline mode"
        OFFLINE=true
    fi
fi

if [ $OFFLINE = true ]
then
    NIFI_SETUP_DIR=$WORKING_DIR/nifi
else
    NIFI_SETUP_DIR=$KYLO_INSTALL_HOME/setup/nifi
fi

echo "Copying the configuration files"
cp $NIFI_SETUP_DIR/nifi.properties $NIFI_INSTALL_HOME/current/conf
sed -i 's/NIFI_LOG_DIR=\".*\"/NIFI_LOG_DIR=\"\/var\/log\/nifi\"/' $NIFI_INSTALL_HOME/current/bin/nifi-env.sh

echo -e "\n\n# Set thinkbig nifi configuration file directory path" >> $NIFI_INSTALL_HOME/current/conf/bootstrap.conf
echo -e "java.arg.15=-Dthinkbig.nifi.configPath=$NIFI_INSTALL_HOME/ext-config" >> $NIFI_INSTALL_HOME/current/conf/bootstrap.conf

echo "Installing the thinkbig libraries to the NiFi lib"
mkdir $NIFI_INSTALL_HOME/current/lib/app
mkdir -p $NIFI_INSTALL_HOME/data/lib/app
cp $NIFI_SETUP_DIR/*.nar $NIFI_INSTALL_HOME/data/lib
cp $NIFI_SETUP_DIR/thinkbig-spark-*.jar $NIFI_INSTALL_HOME/data/lib/app

echo "Creating symbolic links to jar files"
$NIFI_SETUP_DIR/create-symbolic-links.sh

echo "Copy the mysql lib from a lib folder to /opt/nifi/mysql"
mkdir $NIFI_INSTALL_HOME/mysql

if [ $OFFLINE = true ]
then
    cp $NIFI_SETUP_DIR/mysql-connector-java-*.jar $NIFI_INSTALL_HOME/mysql
else
    cp $KYLO_INSTALL_HOME/thinkbig-services/lib/mysql-connector-java-*.jar $NIFI_INSTALL_HOME/mysql
fi

echo "Copy the activeMQ required jars for the JMS processors to /opt/nifi/activemq"
mkdir $NIFI_INSTALL_HOME/activemq
cp $NIFI_SETUP_DIR/activemq/*.jar $NIFI_INSTALL_HOME/activemq

echo "setting up temporary database in case JMS goes down"
mkdir $NIFI_INSTALL_HOME/h2
mkdir $NIFI_INSTALL_HOME/ext-config
cp $NIFI_SETUP_DIR/config.properties $NIFI_INSTALL_HOME/ext-config
chown -R nifi:users $NIFI_INSTALL_HOME

mkdir /var/log/nifi
chown nifi:users /var/log/nifi

echo "Install the nifi service"
cp $NIFI_SETUP_DIR/nifi /etc/init.d
chkconfig nifi on

echo "Starting NiFi service"
service nifi start

echo "Installation Complete"
