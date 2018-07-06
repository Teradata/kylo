#!/bin/bash
KYLO_HOME=$1
VAULT_VERSION=$2
VAULT_INSTALL_HOME=$3
VAULT_USER=$4
VAULT_GROUP=$5
WORKING_DIR=$6
VAULT_DATA_DIR=${VAULT_INSTALL_HOME}/data


VAULT_VERSION="${VAULT_VERSION:-0.10.1}"
UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
VAULT_ZIP="vault_${VAULT_VERSION}_${UNAME}_amd64.zip"
IGNORE_CERTS="${IGNORE_CERTS:-no}"


offline=false

if [ "$7" = "-o" ] || [ "$7" = "-O" ]
then
    echo "Working in offline mode"
    offline=true
fi


argInstructions() {
  cat <<EOF
Incorrect number of arguments.
Arg1 should be the Kylo Home
Arg2 should be the Vault version
Arg3 should be the Vault home
Arg4 should be the Vault user
Arg5 should be the Vault group.
For offline mode pass:
 Arg6 the kylo setup folder
 Arg7 the -o -or -O option
EOF
  exit 1
}
if [ $# -lt 5 ] || [ $# -gt 7 ]; then
    argInstructions
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

echo "Creating Vault data directory '${VAULT_DATA_DIR}'"
mkdir -p ${VAULT_DATA_DIR}

echo "Creating Vault configuration at '${VAULT_INSTALL_HOME}/conf'"
mkdir -p ${VAULT_INSTALL_HOME}/conf

VAULT_PID_DIR=/var/run/vault
echo "Creating Vault PID directory '${VAULT_PID_DIR}'"
mkdir -p ${VAULT_PID_DIR}
VAULT_PID_FILE=${VAULT_PID_DIR}/vault.pid

cat << EOF >> ${VAULT_INSTALL_HOME}/conf/vault.conf
backend "file" {
    path = "${VAULT_DATA_DIR}"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_disable = 1
}

pid_file = "${VAULT_PID_FILE}"
disable_mlock = true
EOF

VAULT_BIN_RUN=${VAULT_INSTALL_HOME}/bin/run.sh
VAULT_BIN_INIT=${VAULT_INSTALL_HOME}/bin/init.sh
VAULT_BIN_UNSEAL=${VAULT_INSTALL_HOME}/bin/unseal.sh
VAULT_BIN_SETUP=${VAULT_INSTALL_HOME}/bin/setup.sh
VAULT_BINARY=${VAULT_INSTALL_HOME}/bin/vault
VAULT_LOG_DIR=/var/log/vault
echo "Creating Vault log directory '${VAULT_LOG_DIR}'"
mkdir -p ${VAULT_LOG_DIR}
cat << EOF > ${VAULT_BIN_RUN}
#!/bin/bash

${VAULT_BINARY} server \
            -config=${VAULT_INSTALL_HOME}/conf/vault.conf \
            >> ${VAULT_LOG_DIR}/vault.log 2>&1 &
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

run() {
    if [ -f ${VAULT_PID_FILE} ]
      then
        echo Already running, process id file exists '${VAULT_PID_FILE}'
      else
        echo Starting Vault ...
        su - \$RUN_AS_USER -c "${VAULT_BIN_RUN}"
    fi
}

unseal() {
    echo Unsealing Vault ...
    su - \$RUN_AS_USER -c "${VAULT_BIN_UNSEAL}"
}

start() {
    if [ -f ${VAULT_PID_FILE} ]
      then
        echo Already running, process id file exists '${VAULT_PID_FILE}'
      else
        run
        unseal
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
    run)
        run
    ;;
    unseal)
        unseal
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
       echo "Restarting Vault"
       stop
       sleep 2
       start
       echo "Vault started"
    ;;
esac
exit 0
EOF

VAULT_ADDRESS="http://localhost:8200"
VAULT_CONF_INIT=${VAULT_INSTALL_HOME}/conf/vault.init
cat << EOF >> ${VAULT_BIN_INIT}
#! /bin/sh
export VAULT_ADDR=${VAULT_ADDRESS}
${VAULT_BINARY} operator init -key-shares=3 -key-threshold=3 | tee ${VAULT_CONF_INIT} > /dev/null
chmod 600 ${VAULT_CONF_INIT}
EOF

cat << EOF >> ${VAULT_BIN_UNSEAL}
#! /bin/sh
export VAULT_ADDR=${VAULT_ADDRESS}
cat ${VAULT_CONF_INIT} | grep '^Unseal' | awk '{print \$4}' | for key in \$(cat -); do
    ${VAULT_BINARY} operator unseal \${key} > ${VAULT_LOG_DIR}/unseal-vault.log 2>&1
done
EOF

cat << EOF >> ${VAULT_BIN_SETUP}
#! /bin/sh
export VAULT_TOKEN=\$1
export VAULT_ADDR=${VAULT_ADDRESS}
echo "Restoring non-versioned K/V backend at secret/"
${VAULT_BINARY} secrets disable secret
${VAULT_BINARY} secrets enable -path secret -version 1 kv
EOF


echo "Assigning owner and group to '$VAULT_USER:$VAULT_GROUP'"
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_INSTALL_HOME}
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_PID_DIR}
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_LOG_DIR}
chmod 700 ${VAULT_PID_DIR}
chmod 700 ${VAULT_INSTALL_HOME}/conf
chmod 600 ${VAULT_INSTALL_HOME}/conf/*
chmod 700 -R ${VAULT_INSTALL_HOME}/bin
chmod 700 ${VAULT_SERVICE_FILE}
chmod 700 ${VAULT_DATA_DIR}

echo "Initialising Vault"
service vault run
sleep 5 # pause for few seconds to let vault start
if ! [ -f ${VAULT_PID_FILE} ]
then
    echo "Vault failed to start, aborting..."
    exit 1;
else
    service vault status
fi
su - ${VAULT_USER} -c "${VAULT_BIN_INIT}"
ROOT_TOKEN=$(cat ${VAULT_CONF_INIT} | grep '^Initial' | awk '{print $4}')
service vault unseal
su - ${VAULT_USER} -c "${VAULT_BIN_SETUP} ${ROOT_TOKEN}"
service vault stop
echo "Vault initialised"

echo "Updating Kylo configuration"
sed -i "s/security\.vault\.token=<insert-vault-secret-token-here>/security\.vault\.token=${ROOT_TOKEN}/" ${KYLO_HOME}/kylo-services/conf/application.properties

echo "Vault installation complete"

instructions() {
  cat <<EOF

Vault has been automatically initialized and unsealed.
The unseal keys and root token have been stored in "${VAULT_CONF_INIT}".
EOF
  exit 1
}

instructions
