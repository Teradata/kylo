#!/bin/bash
VAULT_VERSION=$1
VAULT_INSTALL_HOME=$2
VAULT_USER=$3
VAULT_GROUP=$4
WORKING_DIR=$5

VAULT_VERSION="${VAULT_VERSION:-0.10.1}"
UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
VAULT_ZIP="vault_${VAULT_VERSION}_${UNAME}_amd64.zip"
IGNORE_CERTS="${IGNORE_CERTS:-no}"


offline=false

if [ "$5" = "-o" ] || [ "$5" = "-O" ]
then
    echo "Working in offline mode"
    offline=true
fi

if [ $# -lt 4 ] || [ $# -gt 6 ]; then
    echo "Unknown arguments. Arg1 should be the Vault version, Arg2 should be the Vault home, Arg3 should be the Vault user, Arg4 should be the Vault group. For offline mode pass Arg5 the kylo setup folder and Arg6 the -o -or -O option"
    exit 1
fi

echo "The Vault home folder is $VAULT_INSTALL_HOME using permissions $VAULT_USER:$VAULT_GROUP"

echo "Installing Vault"
mkdir ${VAULT_INSTALL_HOME}
cd ${VAULT_INSTALL_HOME}

if [ ${offline} = true ]
then
    cp ${WORKING_DIR}/vault/${VAULT_ZIP} .
else
    echo "Downloading Vault ${VAULT_VERSION}"
    if [[ "${IGNORE_CERTS}" == "no" ]] ; then
      echo "Downloading Vault with certs verification"
      curl -O "https://releases.hashicorp.com/vault/${VAULT_VERSION}/${VAULT_ZIP}"
    else
      echo "WARNING... Downloading Vault WITHOUT certs verification"
      curl -O "https://releases.hashicorp.com/vault/${VAULT_VERSION}/${VAULT_ZIP}" --no-check-certificate
    fi

    if [[ $? != 0 ]] ; then
      echo "Cannot download Vault"
      exit 1
    fi
fi

if ! [ -f ${VAULT_ZIP} ]
then
    echo "Working in online mode and file '${VAULT_ZIP}' not found.. exiting"
    exit 1
fi

echo "Installing Vault to '${VAULT_INSTALL_HOME}'"
mkdir -p ${VAULT_INSTALL_HOME}/bin
unzip ${VAULT_ZIP}
mv vault bin/
rm -f ${VAULT_ZIP}
rm -Rf vault

echo "Creating Vault configuration at '${VAULT_INSTALL_HOME}/conf'"
mkdir -p ${VAULT_INSTALL_HOME}/conf

VAULT_PID_DIR=/var/run/vault
echo "Creating Vault PID directory '${VAULT_PID_DIR}'"
mkdir -p ${VAULT_PID_DIR}
VAULT_PID_FILE=${VAULT_PID_DIR}/vault.pid

cat << EOF >> ${VAULT_INSTALL_HOME}/conf/vault.conf
backend "inmem" {
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_disable = 1
}

pid_file = "${VAULT_PID_FILE}"
disable_mlock = true
EOF

VAULT_LOG_DIR=/var/log/vault
echo "Creating Vault log directory '${VAULT_LOG_DIR}'"
mkdir -p ${VAULT_LOG_DIR}
cat << EOF > ${VAULT_INSTALL_HOME}/bin/run-vault.sh
#!/bin/bash

${VAULT_INSTALL_HOME}/bin/vault server \
            -config=${VAULT_INSTALL_HOME}/conf/vault.conf \
            -dev \
            -dev-root-token-id="00000000-0000-0000-0000-000000000000" \
            -dev-listen-address="0.0.0.0:8201" \
            &> ${VAULT_LOG_DIR}/vault.log &

exit $?
EOF

VAULT_SERVICE_FILE=/etc/init.d/vault
echo "Creating Vault service at '${VAULT_SERVICE_FILE}'"
# function for determining way to handle startup scripts
function get_linux_type {
# redhat
which chkconfig > /dev/null && echo "chkonfig" && return 0
# ubuntu sysv
which update-rc.d > /dev/null && echo "update-rc.d" && return 0
echo "Couldn't recognize linux version, after installation you need to do these steps manually:"
echo " * add proper header to /etc/init.d/vault file"
echo " * set it to autostart"
}

linux_type=$(get_linux_type)

# header of the service file depends on system used
if [ "$linux_type" == "chkonfig" ]; then
cat << EOF > ${VAULT_SERVICE_FILE}
#! /bin/sh
# chkconfig: 345 98 20
# description: vault
# processname: vault
EOF
elif [ "$linux_type" == "update-rc.d" ]; then
cat << EOF > ${VAULT_SERVICE_FILE}
#! /bin/sh
### BEGIN INIT INFO
# Provides:          vault
# Required-Start:    $local_fs $network $named $time $syslog
# Required-Stop:     $local_fs $network $named $time $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       vault
### END INIT INFO
EOF
fi

cat << EOF >> ${VAULT_SERVICE_FILE}
RUN_AS_USER=${VAULT_USER}

start() {
    if [ -f ${VAULT_PID_FILE} ]
      then
        echo Already running, process id file exists '${VAULT_PID_FILE}'
      else
        echo Starting Vault ...
        su - \$RUN_AS_USER -c "${VAULT_INSTALL_HOME}/bin/run-vault.sh"
    fi
}

stop() {
    if [ -f ${VAULT_PID_FILE} ]
      then
        echo Stopping Vault ...
        kill -15 \$(cat ${VAULT_PID_FILE})
      else
        echo Already stopped, process id does not exist '${VAULT_PID_FILE}'
    fi
}

status() {
    if [ -f ${VAULT_PID_FILE} ]
      then
          echo Running. Here are the related process:
          ps -f -p \$(cat ${VAULT_PID_FILE})
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
       echo "Restarting Vault"
       stop
       sleep 2
       start
       echo "Vault started"
    ;;
esac
exit 0
EOF

echo "Assigning owner and group to '$VAULT_USER:$VAULT_GROUP'"
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_INSTALL_HOME}
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_PID_DIR}
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_LOG_DIR}
chmod u+x ${VAULT_INSTALL_HOME}/bin/*
chmod u+x ${VAULT_SERVICE_FILE}

echo "Vault installation complete"
