#!/bin/bash

KYLO_OFFLINE=false
NIFI_INSTALL_HOME=$1
KYLO_INSTALL_HOME=$2
NIFI_USER=$3
NIFI_GROUP=$4
KYLO_WORKING_DIR=$5

if [ $# -eq 4 ]
then
    echo "The NIFI home folder is $1 and the Kylo home folder is $2 using permissions  $NIFI_USER:$NIFI_GROUP"
elif [ $# -eq 6 ] && ([ "$6" = "-o" ] || [ "$6" = "-O" ])
then
    echo "Working in offline mode"
        KYLO_OFFLINE=true
else
    echo "Unknown arguments. The first argument should be the path to the nifi home folder and the second argument should be the kylo home. To enable offline mode pass the Kylo setup folder and the -o option last "
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

if [ $KYLO_OFFLINE = true ]
then
    NIFI_SETUP_DIR=$KYLO_WORKING_DIR/nifi
else
    NIFI_SETUP_DIR=$KYLO_INSTALL_HOME/setup/nifi
fi

echo -e "\n\n# Set kylo nifi configuration file directory path" >> $NIFI_INSTALL_HOME/current/conf/bootstrap.conf
echo -e "java.arg.15=-Dkylo.nifi.configPath=$NIFI_INSTALL_HOME/ext-config" >> $NIFI_INSTALL_HOME/current/conf/bootstrap.conf

echo "Installing the kylo libraries to the NiFi lib"
mkdir $NIFI_INSTALL_HOME/current/lib/app
mkdir -p $NIFI_INSTALL_HOME/data/lib/app
cp $NIFI_SETUP_DIR/*.nar $NIFI_INSTALL_HOME/data/lib
cp $NIFI_SETUP_DIR/kylo-spark-*.jar $NIFI_INSTALL_HOME/data/lib/app

echo "Creating symbolic links to jar files"
$NIFI_SETUP_DIR/create-symbolic-links.sh $NIFI_INSTALL_HOME $NIFI_USER $NIFI_GROUP

echo "Copy the mysql lib from a lib folder to $NIFI_INSTALL_HOME/mysql"
mkdir $NIFI_INSTALL_HOME/mysql

if [ $KYLO_OFFLINE = true ]
then
    cp $NIFI_SETUP_DIR/mariadb-java-client-*.jar $NIFI_INSTALL_HOME/mysql
else
    cp $KYLO_INSTALL_HOME/kylo-services/lib/mariadb-java-client-*.jar $NIFI_INSTALL_HOME/mysql
fi

echo "Copy the activeMQ required jars for the JMS processors to $NIFI_INSTALL_HOME/activemq"
mkdir $NIFI_INSTALL_HOME/activemq
cp $NIFI_SETUP_DIR/activemq/*.jar $NIFI_INSTALL_HOME/activemq

echo "setting up temporary database in case JMS goes down"
mkdir $NIFI_INSTALL_HOME/h2
mkdir $NIFI_INSTALL_HOME/ext-config
cp $NIFI_SETUP_DIR/config.properties $NIFI_INSTALL_HOME/ext-config
chown -R $NIFI_USER:$NIFI_GROUP $NIFI_INSTALL_HOME

echo "Creating flow file cache directory"
mkdir $NIFI_INSTALL_HOME/feed_flowfile_cache/
chown $NIFI_USER:$NIFI_GROUP $NIFI_INSTALL_HOME/feed_flowfile_cache/

mkdir /var/log/nifi
chown $NIFI_USER:$NIFI_GROUP /var/log/nifi

echo "Install the nifi service"
cp $NIFI_SETUP_DIR/nifi /etc/init.d

echo "Updating the home folder for the init.d script"
sed -i "s|dir=\"\/opt\/nifi\/current\/bin\"|dir=\"$NIFI_INSTALL_HOME\/current\/bin\"|" /etc/init.d/nifi
sed -i "s|RUN_AS_USER=nifi|RUN_AS_USER=$NIFI_USER|" /etc/init.d/nifi

echo "Updating the provenance cache location"
sed -i "s|kylo.provenance.cache.location=\/opt\/nifi\/feed-event-statistics.gz|kylo.provenance.cache.location=$NIFI_INSTALL_HOME\/feed-event-statistics.gz|" $NIFI_INSTALL_HOME/ext-config/config.properties

if [ "$linux_type" == "chkonfig" ]; then
    chkconfig nifi on
elif [ "$linux_type" == "update-rc.d" ]; then
    update-rc.d nifi defaults 98 10
fi

echo "Installation Complete"
