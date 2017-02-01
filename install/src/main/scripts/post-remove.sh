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
rpmInstallDir=/opt/kylo

echo "     2. Stopping Applications ... "
service kylo-ui stop
service kylo-services stop
service kylo-spark-shell stop

echo "     3. Removing service configuration "
chkconfig --del kylo-ui
rm -rf /etc/init.d/kylo-ui
echo "         - Removed kylo-ui script '/etc/init.d/kylo-ui'"
chkconfig --del kylo-services
rm -rf /etc/init.d/kylo-services
echo "         - Removed kylo-services script '/etc/init.d/kylo-services'"
chkconfig --del kylo-spark-shell
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

echo "    REMOVAL COMPLETE! "
