#!/bin/bash

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

read -p "Are you sure you want to uninstall all Kylo components AND delete the kylo database? Type YES to confirm: " CONFIRM_DELETE

if [ "$CONFIRM_DELETE" == "YES" ] ; then

echo "Uninstalling all components that were installed from the RPM and setup-wizard"
echo "Uninstalling kylo applications with RPM uninstall"
/opt/kylo/remove-kylo.sh
rm -rf /opt/kylo
mysql -phadoop -e "drop database kylo;"
mysql -phadoop -e "show databases;"

echo "Uninstalling NiFi"
service nifi stop
if [ "$linux_type" == "chkonfig" ]; then
    chkconfig nifi off
fi

rm -rf /opt/nifi
rm -rf /var/log/nifi
rm -f /etc/init.d/nifi

echo "Uninstalling ActiveMQ"
service activemq stop
rm -f /etc/init.d/activemq
rm -f /etc/default/activemq
rm -rf /opt/activemq

echo "Uninstalling elasticsearch"
if [ "$linux_type" == "chkonfig" ]; then
    rpm -e elasticsearch
elif [ "$linux_type" == "update-rc.d" ]; then
    dpkg -r elasticsearch
fi

rm -rf /var/lib/elasticsearch/
rm -rf /etc/elasticsearch/
rm -rf /usr/share/elasticsearch/

echo "Uninstalling /opt/java"
rm -rf /opt/java

echo "Uninstall complete. You should now be able to re-install the RPM and run the setup wizard to get a clean install. Note: The users were not deleted. You can skip the manual step of adding a user"

else 
  echo "Exiting and skipping removal since you didn't say YES"
fi

# Dont delete users by default. It causes issues if you are re-installing everything
#userdel kylo
#userdel nifi
#userdel activemq