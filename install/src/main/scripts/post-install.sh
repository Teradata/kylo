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

chown -R kylo:users /opt/kylo

rpmInstallDir=/opt/kylo
pgrepMarkerKyloUi=kylo-ui-pgrep-marker
pgrepMarkerKyloServices=kylo-services-pgrep-marker
pgrepMarkerKyloSparkShell=kylo-spark-shell-pgrep-marker
rpmLogDir=/var/log

echo "    - Install kylo-ui application"
tar -xf $rpmInstallDir/kylo-ui/kylo-ui-app-*.tar.gz -C $rpmInstallDir/kylo-ui --strip-components=1
rm -rf $rpmInstallDir/kylo-ui/kylo-ui-app-*.tar.gz

jwtkey=$(head -c 64 /dev/urandom | md5sum |cut -d' ' -f1)
sed -i "s/security\.jwt\.key=<insert-256-bit-secret-key-here>/security\.jwt\.key=${jwtkey}/" $rpmInstallDir/kylo-ui/conf/application.properties
echo "   - Installed kylo-ui to '$rpmInstallDir/kylo-ui'"

if ! [ -f $rpmInstallDir/encrypt.key ]
then
    head -c64 < /dev/urandom | base64 > $rpmInstallDir/encrypt.key
    chmod 400 $rpmInstallDir/encrypt.key
    chown kylo:users $rpmInstallDir/encrypt.key
fi

cat << EOF > $rpmInstallDir/kylo-ui/bin/run-kylo-ui.sh
#!/bin/bash
export JAVA_HOME=/opt/java/current
export PATH=\$JAVA_HOME/bin:\$PATH
export KYLO_UI_OPTS=-Xmx512m
[ -f $rpmInstallDir/encrypt.key ] && export ENCRYPT_KEY="\$(cat $rpmInstallDir/encrypt.key)"
java \$KYLO_UI_OPTS -cp $rpmInstallDir/kylo-ui/conf:$rpmInstallDir/kylo-ui/lib/*:$rpmInstallDir/kylo-ui/plugin/* com.thinkbiganalytics.KyloUiApplication --pgrep-marker=$pgrepMarkerKyloUi > /var/log/kylo-ui/kylo-ui.log 2>&1 &
EOF
cat << EOF > $rpmInstallDir/kylo-ui/bin/run-kylo-ui-with-debug.sh
  #!/bin/bash
export JAVA_HOME=/opt/java/current
export PATH=\$JAVA_HOME/bin:\$PATH
export KYLO_UI_OPTS=-Xmx512m
[ -f $rpmInstallDir/encrypt.key ] && export ENCRYPT_KEY="\$(cat $rpmInstallDir/encrypt.key)"
JAVA_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9997
java \$KYLO_UI_OPTS \$JAVA_DEBUG_OPTS -cp $rpmInstallDir/kylo-ui/conf:$rpmInstallDir/kylo-ui/lib/*:$rpmInstallDir/kylo-ui/plugin/* com.thinkbiganalytics.KyloUiApplication --pgrep-marker=$pgrepMarkerKyloUi > /var/log/kylo-ui/kylo-ui.log 2>&1 &
EOF
chmod +x $rpmInstallDir/kylo-ui/bin/run-kylo-ui.sh
chmod +x $rpmInstallDir/kylo-ui/bin/run-kylo-ui-with-debug.sh
echo "   - Created kylo-ui script '$rpmInstallDir/kylo-ui/bin/run-kylo-ui.sh'"

cat << EOF > /etc/init.d/kylo-ui
#! /bin/sh
# chkconfig: 345 98 22
# description: kylo-ui
# processname: kylo-ui
RUN_AS_USER=kylo

debug() {
    if pgrep -f kylo-ui-pgrep-marker >/dev/null 2>&1
      then
        echo Already running.
      else
        echo Starting kylo-ui in debug mode...
        grep 'address=' $rpmInstallDir/kylo-ui/bin/run-kylo-ui-with-debug.sh
        su - \$RUN_AS_USER -c "$rpmInstallDir/kylo-ui/bin/run-kylo-ui-with-debug.sh"
    fi
}

start() {
    if pgrep -f $pgrepMarkerKyloUi >/dev/null 2>&1
      then
        echo Already running.
      else
        echo Starting kylo-ui ...
        su - \$RUN_AS_USER -c "$rpmInstallDir/kylo-ui/bin/run-kylo-ui.sh"
    fi
}

stop() {
    if pgrep -f $pgrepMarkerKyloUi >/dev/null 2>&1
      then
        echo Stopping kylo-ui ...
        pkill -f $pgrepMarkerKyloUi
      else
        echo Already stopped.
    fi
}

status() {
    if pgrep -f $pgrepMarkerKyloUi >/dev/null 2>&1
      then
          echo Running.  Here are the related processes:
          pgrep -lf $pgrepMarkerKyloUi
      else
        echo Stopped.
    fi
}

case "\$1" in
    debug)
        debug
    ;;
    start)
        start
    ;;
    stop)
        stop
    ;;
    status)
        status
    ;;
    restart)
       echo "Restarting kylo-ui"
       stop
       sleep 2
       start
       echo "kylo-ui started"
    ;;
esac
exit 0
EOF
chmod +x /etc/init.d/kylo-ui
echo "   - Created kylo-ui script '/etc/init.d/kylo-ui'"

mkdir -p $rpmLogDir/kylo-ui/
echo "   - Created Log folder $rpmLogDir/kylo-ui/"

chkconfig --add kylo-ui
chkconfig kylo-ui on
echo "   - Added service 'kylo-ui'"
echo "    - Completed kylo-ui install"

echo "    - Install kylo-services application"

tar -xf $rpmInstallDir/kylo-services/kylo-service-app-*.tar.gz -C $rpmInstallDir/kylo-services --strip-components=1
tar -xf $rpmInstallDir/kylo-services/kylo-nifi-rest-client-v1-*.tar.gz -C $rpmInstallDir/kylo-services --strip-components=1
rm -rf $rpmInstallDir/kylo-services/kylo-service-app-*.tar.gz
rm -rf $rpmInstallDir/kylo-services/kylo-nifi-rest-client-v1-*.tar.gz
rm -f $rpmInstallDir/kylo-services/lib/jetty*
rm -f $rpmInstallDir/kylo-services/lib/servlet-api*
sed -i "s/security\.jwt\.key=<insert-256-bit-secret-key-here>/security\.jwt\.key=${jwtkey}/" $rpmInstallDir/kylo-services/conf/application.properties
echo "   - Installed kylo-services to '$rpmInstallDir/kylo-services'"

cat << EOF > $rpmInstallDir/kylo-services/bin/run-kylo-services.sh
#!/bin/bash
export JAVA_HOME=/opt/java/current
export PATH=\$JAVA_HOME/bin:\$PATH
export KYLO_SERVICES_OPTS=-Xmx768m
[ -f $rpmInstallDir/encrypt.key ] && export ENCRYPT_KEY="\$(cat $rpmInstallDir/encrypt.key)"
KYLO_NIFI_PROFILE=\$(grep ^spring.profiles. $rpmInstallDir/kylo-services/conf/application.properties | grep -o nifi-v.)
java \$KYLO_SERVICES_OPTS -cp $rpmInstallDir/kylo-services/conf:$rpmInstallDir/kylo-services/lib/*:$rpmInstallDir/kylo-services/lib/\${KYLO_NIFI_PROFILE}/*:$rpmInstallDir/kylo-services/plugin/* com.thinkbiganalytics.server.KyloServerApplication --pgrep-marker=$pgrepMarkerKyloServices > /var/log/kylo-services/kylo-services.log 2>&1 &
EOF
cat << EOF > $rpmInstallDir/kylo-services/bin/run-kylo-services-with-debug.sh
#!/bin/bash
export JAVA_HOME=/opt/java/current
export PATH=\$JAVA_HOME/bin:\$PATH
export KYLO_SERVICES_OPTS=-Xmx768m
[ -f $rpmInstallDir/encrypt.key ] && export ENCRYPT_KEY="\$(cat $rpmInstallDir/encrypt.key)"
JAVA_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9998
KYLO_NIFI_PROFILE=\$(grep ^spring.profiles. $rpmInstallDir/kylo-services/conf/application.properties | grep -o nifi-v.)
java \$KYLO_SERVICES_OPTS \$JAVA_DEBUG_OPTS -cp $rpmInstallDir/kylo-services/conf:$rpmInstallDir/kylo-services/lib/*:$rpmInstallDir/kylo-services/lib/\${KYLO_NIFI_PROFILE}/*:$rpmInstallDir/kylo-services/plugin/* com.thinkbiganalytics.server.KyloServerApplication --pgrep-marker=$pgrepMarkerKyloServices > /var/log/kylo-services/kylo-services.log 2>&1 &
EOF
chmod +x $rpmInstallDir/kylo-services/bin/run-kylo-services.sh
chmod +x $rpmInstallDir/kylo-services/bin/run-kylo-services-with-debug.sh
echo "   - Created kylo-services script '$rpmInstallDir/kylo-services/bin/run-kylo-services.sh'"

cat << EOF > /etc/init.d/kylo-services
#! /bin/sh
# chkconfig: 345 98 21
# description: kylo-services
# processname: kylo-services
RUN_AS_USER=kylo

debug() {
    if pgrep -f kylo-services-pgrep-marker >/dev/null 2>&1
      then
        echo Already running.
      else
        echo Starting kylo-services in debug mode...
        grep 'address=' $rpmInstallDir/kylo-services/bin/run-kylo-services-with-debug.sh
        su - \$RUN_AS_USER -c "$rpmInstallDir/kylo-services/bin/run-kylo-services-with-debug.sh"
    fi
}

start() {
    if pgrep -f $pgrepMarkerKyloServices >/dev/null 2>&1
      then
        echo Already running.
      else
        echo Starting kylo-services ...
        su - \$RUN_AS_USER -c "$rpmInstallDir/kylo-services/bin/run-kylo-services.sh"
    fi
}

stop() {
    if pgrep -f $pgrepMarkerKyloServices >/dev/null 2>&1
      then
        echo Stopping kylo-services ...
        pkill -f $pgrepMarkerKyloServices
      else
        echo Already stopped.
    fi
}

status() {
    if pgrep -f $pgrepMarkerKyloServices >/dev/null 2>&1
      then
          echo Running.  Here are the related processes:
          pgrep -lf $pgrepMarkerKyloServices
      else
        echo Stopped.
    fi
}

case "\$1" in
    debug)
        debug
    ;;
    start)
        start
    ;;
    stop)
        stop
    ;;
    status)
        status
    ;;
    restart)
       echo "Restarting kylo-services"
       stop
       sleep 2
       start
       echo "kylo-services started"
    ;;
esac
exit 0
EOF
chmod +x /etc/init.d/kylo-services
echo "   - Created kylo-services script '/etc/init.d/kylo-services'"

mkdir -p $rpmLogDir/kylo-services/
echo "   - Created Log folder $rpmLogDir/kylo-services/"

chkconfig --add kylo-services
chkconfig kylo-services on
echo "   - Added service 'kylo-services'"


echo "    - Completed kylo-services install"

echo "    - Install kylo-spark-shell application"

cat << EOF > $rpmInstallDir/kylo-services/bin/run-kylo-spark-shell.sh
#!/bin/bash
SPARK_PROFILE="v"\$(spark-submit --version 2>&1 | grep -o "version [0-9]" | grep -o "[0-9]" | head -1)
spark-submit --conf spark.driver.userClassPathFirst=true --class com.thinkbiganalytics.spark.SparkShellApp --driver-class-path /opt/kylo/kylo-services/conf --driver-java-options -Dlog4j.configuration=log4j-spark.properties $rpmInstallDir/kylo-services/lib/app/kylo-spark-shell-client-\${SPARK_PROFILE}-*.jar --pgrep-marker=$pgrepMarkerKyloSparkShell
EOF
chmod +x $rpmInstallDir/kylo-services/bin/run-kylo-spark-shell.sh
echo "   - Created kylo-spark-shell script '$rpmInstallDir/kylo-services/bin/run-kylo-spark-shell.sh'"

cat << EOF > /etc/init.d/kylo-spark-shell
#! /bin/sh
# chkconfig: 345 98 20
# description: kylo-spark-shell
# processname: kylo-spark-shell
stdout_log="/var/log/kylo-services/kylo-spark-shell.log"
stderr_log="/var/log/kylo-services/kylo-spark-shell.err"
RUN_AS_USER=kylo

start() {
    if pgrep -f $pgrepMarkerKyloSparkShell >/dev/null 2>&1
      then
        echo Already running.
      else
        echo Starting kylo-spark-shell ...
        su - \$RUN_AS_USER -c "$rpmInstallDir/kylo-services/bin/run-kylo-spark-shell.sh >> \$stdout_log 2>> \$stderr_log" &
    fi
}

stop() {
    if pgrep -f $pgrepMarkerKyloSparkShell >/dev/null 2>&1
      then
        echo Stopping kylo-spark-shell ...
        pkill -f $pgrepMarkerKyloSparkShell
      else
        echo Already stopped.
    fi
}

status() {
    if pgrep -f $pgrepMarkerKyloSparkShell >/dev/null 2>&1
      then
          echo Running.  Here are the related processes:
          pgrep -lf $pgrepMarkerKyloSparkShell
      else
        echo Stopped.
    fi
}

case "\$1" in
    start)
        start
    ;;
    stop)
        stop
    ;;
    status)
        status
    ;;
    restart)
       echo "Restarting kylo-spark-shell"
       stop
       sleep 2
       start
       echo "kylo-spark-shell started"
    ;;
esac
exit 0
EOF
chmod +x /etc/init.d/kylo-spark-shell
echo "   - Created kylo-spark-shell script '/etc/init.d/kylo-spark-shell'"

chkconfig --add kylo-spark-shell
chkconfig kylo-spark-shell on
echo "   - Added service 'kylo-spark-shell'"


echo "    - Completed kylo-spark-shell install"

{
echo "    - Create an RPM Removal script at: $rpmInstallDir/remove-kylo.sh"
lastRpm=$(rpm -qa | grep kylo)
touch $rpmInstallDir/remove-kylo.sh
echo "rpm -e $lastRpm " > $rpmInstallDir/remove-kylo.sh
chmod +x $rpmInstallDir/remove-kylo.sh

}

chown -R kylo:kylo /opt/kylo

chmod 744 $rpmLogDir/kylo*

chown kylo:kylo $rpmLogDir/kylo*

echo "   INSTALL COMPLETE"
echo "   - Please configure the application using the property files and scripts located under the '$rpmInstallDir/kylo-ui/conf' and '$rpmInstallDir/kylo-services/conf' folder.  See deployment guide for details."
echo "   - To remove kylo run $rpmInstallDir/remove-kylo.sh "
