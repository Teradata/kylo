#!/bin/bash
${NIFI_SETUP_DIR=/opt/thinkbig/setup/nifi}

echo "Copying the configuration files"
cp $NIFI_SETUP_DIR/logback.xml /opt/nifi/current/conf
cp $NIFI_SETUP_DIR/nifi.properties /opt/nifi/current/conf
cp $NIFI_SETUP_DIR/bootstrap.conf /opt/nifi/current/conf

echo "Installing the thinkbig libraries to the NiFi lib"
mkdir /opt/nifi/current/lib/app
cp $NIFI_SETUP_DIR/*.nar /opt/nifi/current/lib
cp $NIFI_SETUP_DIR/thinkbig-spark-*.jar /opt/nifi/current/lib/app
echo "Copy the mysql lib from a lib folder to /opt/nifi/mysql"
mkdir /opt/nifi/mysql
cp /opt/thinkbig/thinkbig-services/lib/mysql-connector-java-*.jar /opt/nifi/mysql

echo "setting up temporary database in case JMS goes down"
mkdir /opt/nifi/h2
mkdir /opt/nifi/ext-config
cp $NIFI_SETUP_DIR/config.properties /opt/nifi/ext-config
chown -R nifi:users /opt/nifi

mkdir /var/log/nifi
chown nifi:users /var/log/nifi

echo "Install the nifi service"
cp $NIFI_SETUP_DIR/nifi /etc/init.d
chkconfig nifi on

echo "Starting NiFi service"
service nifi start

echo "Installation Complete"
