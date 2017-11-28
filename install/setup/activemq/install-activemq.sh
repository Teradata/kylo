#!/bin/bash
#########
#  ActiveMQ requires Java 8 in the latest versions

#  Example 1: ./install-activemq.sh /opt/activemq
#  Example 2: ./install-activemq.sh /opt/activemq /tmp/offline-install -o
#########
#Note: edit /etc/default/activemq to change Java memory parameters

offline=false
KYLO_SETUP_FOLDER=/opt/kylo/setup
ACTIVEMQ_INSTALL_HOME=/opt/activemq
ACTIVEMQ_USER=activemq
ACTIVEMQ_GROUP=users
ACTIVEMQ_JAVA_HOME=""
ACTIVEMQ_INSTALL_VERSION=5.15.0

if [ $# -eq 4 ]
then
    echo "The active home folder is $1 "
    ACTIVEMQ_INSTALL_HOME=$1
    ACTIVEMQ_USER=$2
    ACTIVEMQ_GROUP=$3
    ACTIVEMQ_JAVA_HOME=$4
elif [ $# -eq 6 ] && ([ "$6" = "-o" ] || [ "$6" = "-O" ])
then
    echo "Working in offline mode"
    offline=true
    ACTIVEMQ_INSTALL_HOME=$1
    ACTIVEMQ_USER=$2
    ACTIVEMQ_GROUP=$3
    ACTIVEMQ_JAVA_HOME=$4
    KYLO_SETUP_FOLDER=$5
else
    echo "Unknown arguments. The first argument should be the path to the activemq home folder. Optional you can pass a second argument to set offline mode. The value is -o or -O "
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

echo "Create the $ACTIVEMQ_INSTALL_HOME directory"
mkdir -p $ACTIVEMQ_INSTALL_HOME

if [ $offline = true ]
then
    cp $KYLO_SETUP_FOLDER/activemq/apache-activemq-$ACTIVEMQ_INSTALL_VERSION-bin.tar.gz $ACTIVEMQ_INSTALL_HOME
    cd $ACTIVEMQ_INSTALL_HOME
else
    echo "Download activemq and install"
    cd $ACTIVEMQ_INSTALL_HOME
    curl -O -k https://archive.apache.org/dist/activemq/$ACTIVEMQ_INSTALL_VERSION/apache-activemq-$ACTIVEMQ_INSTALL_VERSION-bin.tar.gz
fi


if ! [ -f apache-activemq-$ACTIVEMQ_INSTALL_VERSION-bin.tar.gz ]
then
    echo "Installation file not found.. exiting"
    exit 1
fi

tar -xvf apache-activemq-$ACTIVEMQ_INSTALL_VERSION-bin.tar.gz
rm -f apache-activemq-$ACTIVEMQ_INSTALL_VERSION-bin.tar.gz
ln -s apache-activemq-$ACTIVEMQ_INSTALL_VERSION current

echo "Installing as a service"
# http://activemq.apache.org/unix-shell-script.html

chown -R $ACTIVEMQ_USER:$ACTIVEMQ_GROUP $ACTIVEMQ_INSTALL_HOME
cp $ACTIVEMQ_INSTALL_HOME/current/bin/env /etc/default/activemq

if [ -z "$ACTIVEMQ_JAVA_HOME" ] || ["$ACTIVEMQ_JAVA_HOME" == "SYSTEM_JAVA" ]
then
 echo "No Java home has been specified for ActiveMQ. Using the system Java home"
else
  sed -i "/\#\!\/bin\/sh/a export JAVA_HOME=$ACTIVEMQ_JAVA_HOME" /etc/default/activemq
fi

sed -i "~s|^ACTIVEMQ_USER=\"\"|ACTIVEMQ_USER=\"$ACTIVEMQ_USER\"|" /etc/default/activemq
chmod 644 /etc/default/activemq
ln -snf  $ACTIVEMQ_INSTALL_HOME/current/bin/activemq /etc/init.d/activemq

if [ "$linux_type" == "chkonfig" ]; then
    chkconfig --add activemq
    chkconfig activemq on
elif [ "$linux_type" == "update-rc.d" ]; then
    update-rc.d activemq defaults 95 10
fi

service activemq start
