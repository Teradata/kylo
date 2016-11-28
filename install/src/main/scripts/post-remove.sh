#!/bin/bash
rpmInstallDir=/opt/thinkbig

echo "     2. Stopping Applications ... "
service thinkbig-ui stop
service thinkbig-services stop
service thinkbig-spark-shell stop

echo "     3. Removing service configuration "
chkconfig --del thinkbig-ui
rm -rf /etc/init.d/thinkbig-ui
echo "         - Removed thinkbig-ui script '/etc/init.d/thinkbig-ui'"
chkconfig --del thinkbig-services
rm -rf /etc/init.d/thinkbig-services
echo "         - Removed thinkbig-services script '/etc/init.d/thinkbig-services'"
chkconfig --del thinkbig-spark-shell
rm -rf /etc/init.d/thinkbig-spark-shell
echo "         - Removed thinkbig-spark-shell script '/etc/init.d/thinkbig-spark-shell'"
rm -rf $rpmInstallDir/setup

echo "     4. Deleting application folders "
rm -rf $rpmInstallDir/thinkbig-ui
echo "         - Removed thinkbig-ui"
rm -rf $rpmInstallDir/thinkbig-services
echo "         - Removed thinkbig-services"

echo "     5. Deleting log folders "
rm -rf /var/log/thinkbig-ui
rm -rf /var/log/thinkbig-services

echo "    REMOVAL COMPLETE! "
