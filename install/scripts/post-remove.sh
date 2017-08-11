#!/bin/bash

###
# #%L
# install
# %%
# Copyright (C) 2017 ThinkBig Analytics
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###

# function for determining way to handle startup scripts
function get_linux_type {
# redhat
which chkconfig > /dev/null && echo "chkonfig" && return 0
# ubuntu sysv
which update-rc.d > /dev/null && echo "update-rc.d" && return 0
echo "Couldn't recognize linux version, after removal you need to turn off autostart of kylo services
(kylo-ui, kylo-services and kylo-spark-shell)"
}

linux_type=$(get_linux_type)
echo "Type of init scripts management tool determined as $linux_type"

rpmInstallDir=/opt/kylo

echo "     2. Stopping Applications ... "
service kylo-ui stop
service kylo-services stop
service kylo-spark-shell stop

echo "     3. Removing service configuration "

if [ "$linux_type" == "chkonfig" ]; then
    chkconfig --del kylo-ui
    chkconfig --del kylo-spark-shell
    chkconfig --del kylo-services
elif [ "$linux_type" == "update-rc.d" ]; then
    update-rc.d -f kylo-ui remove
    update-rc.d -f kylo-shell remove
    update-rc.d -f kylo-spark-services remove
fi
rm -rf /etc/init.d/kylo-ui
echo "         - Removed kylo-ui script '/etc/init.d/kylo-ui'"
rm -rf /etc/init.d/kylo-services
echo "         - Removed kylo-services script '/etc/init.d/kylo-services'"
rm -rf /etc/init.d/kylo-spark-shell
echo "         - Removed kylo-spark-shell script '/etc/init.d/kylo-spark-shell'"

rm -rf $rpmInstallDir/setup

echo "     4. Deleting application folders "
rm -rf $rpmInstallDir/kylo-ui
echo "         - Removed kylo-ui"
rm -rf $rpmInstallDir/kylo-services
echo "         - Removed kylo-services"

echo "     5. Deleting log folders "
rm -rf /var/log/kylo-ui
rm -rf /var/log/kylo-services
rm -rf /var/log/kylo-spark-shell

echo "     6. Deleting kylo-service "
rm -rf /usr/bin/kylo-service

echo "    REMOVAL COMPLETE! "
