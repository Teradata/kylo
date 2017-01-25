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
