#!/bin/bash
KYLO_HOME=$1
KYLO_USER=$2
KYLO_GROUP=$3
VAULT_VERSION=$4
VAULT_INSTALL_HOME=$5
VAULT_USER=$6
VAULT_GROUP=$7
WORKING_DIR=$8
OFFLINE_MODE=$9
VAULT_DATA_DIR=${VAULT_INSTALL_HOME}/data


VAULT_VERSION="${VAULT_VERSION:-0.9.0}"
UNAME=$(uname -s |  tr '[:upper:]' '[:lower:]')
VAULT_BINARY_NAME="vault_${VAULT_VERSION}_${UNAME}_amd64"
VAULT_ZIP="${VAULT_BINARY_NAME}.zip"
IGNORE_CERTS="${IGNORE_CERTS:-no}"

VAULT_CURRENT=${VAULT_INSTALL_HOME}/current
VAULT_CURRENT_HOME=${VAULT_INSTALL_HOME}/${VAULT_BINARY_NAME}
VAULT_BINARY=${VAULT_CURRENT}/bin/vault
VAULT_BIN_RUN=${VAULT_CURRENT}/bin/run.sh
VAULT_BIN_INIT=${VAULT_CURRENT}/bin/init.sh
VAULT_BIN_UNSEAL=${VAULT_CURRENT}/bin/unseal.sh
VAULT_BIN_SETUP=${VAULT_CURRENT}/bin/setup.sh
VAULT_LOG_DIR=/var/log/vault
VAULT_PORT="8200"
VAULT_HOST="localhost"
VAULT_ADDRESS="https://${VAULT_HOST}:${VAULT_PORT}"
VAULT_CONF=${VAULT_CURRENT_HOME}/conf
VAULT_CONF_INIT=${VAULT_CONF}/vault.init
VAULT_CERT_PEM=${VAULT_CONF}/vault-cert.pem
VAULT_KEY_PEM=${VAULT_CONF}/vault-key.pem
VAULT_CA_CERT_PEM=${VAULT_CONF}/ca-cert.pem
VAULT_ROOT_CERT_PEM=${VAULT_CONF}/root-cert.pem
VAULT_TRUSTSTORE=${VAULT_CONF}/truststore.jks
VAULT_PID_DIR=/var/run/vault
VAULT_PID_FILE=${VAULT_PID_DIR}/vault.pid
VAULT_SERVICE_FILE=/etc/init.d/vault

KYLO_HOST=localhost
KYLO_SSL=${KYLO_HOME}/ssl
KYLO_TRUSTSTORE=${KYLO_SSL}/kylo-vault-truststore.jks
KYLO_KEYSTORE=${KYLO_SSL}/kylo-vault-keystore.jks
KYLO_CERT_PEM=${KYLO_SSL}/kylo-cert.pem


offline=false

if [ "${OFFLINE_MODE}" = "-o" ] || [ "${OFFLINE_MODE}" = "-O" ]
then
    echo "Working in offline mode"
    offline=true
fi


argInstructions() {
  cat <<EOF
Incorrect number of arguments.
Arg1 should be the Kylo Home
Arg2 should be the Kylo User
Arg3 should be the Kylo Group
Arg4 should be the Vault version
Arg5 should be the Vault home
Arg6 should be the Vault user
Arg7 should be the Vault group.
For offline mode pass:
 Arg8 the kylo setup folder
 Arg9 the -o -or -O option
EOF
  exit 1
}
if [ $# -lt 7 ] || [ $# -gt 9 ]; then
    argInstructions
    exit 1
fi

echo "The Vault home folder is $VAULT_INSTALL_HOME"
echo "Using permissions $VAULT_USER:$VAULT_GROUP"

if ! id -u ${VAULT_USER} >/dev/null 2>&1; then
        echo "Vault user '${VAULT_USER}' does not exist, aborting..."
        exit 1
fi

PROPS=${KYLO_HOME}/kylo-services/conf/application.properties
if ! grep "vault.host" ${PROPS} > /dev/null
then
   echo "ERROR: Vault properties not found in ${PROPS}. Follow upgrade instructions at https://kylo.readthedocs.io/en/latest/release-notes/ReleaseNotes9.2.html"
   exit 1
fi


echo "Installing Vault"
mkdir ${VAULT_INSTALL_HOME}
mkdir ${VAULT_CURRENT_HOME}
mkdir ${VAULT_CURRENT_HOME}/bin
mkdir ${VAULT_CURRENT_HOME}/conf
cd ${VAULT_INSTALL_HOME}

if [ ${offline} = true ]
then
    echo "who am i: $(whoami)"
    echo "working dir: ${WORKING_DIR}"
    echo "pwd: $(pwd)"
    cp ${WORKING_DIR}/vault/${VAULT_ZIP} .
else
    URL="https://releases.hashicorp.com/vault/${VAULT_VERSION}/${VAULT_ZIP}"
    echo "Downloading Vault ${VAULT_VERSION} from ${URL}"
    if [[ "${IGNORE_CERTS}" == "no" ]] ; then
      echo "Downloading Vault with certs verification, to download without cert verification 'export IGNORE_CERTS=yes' before running this script"
      curl -O ${URL}
    else
      echo "WARNING... Downloading Vault WITHOUT certs verification"
      curl -O ${URL} --insecure
    fi

    if [[ $? != 0 ]] ; then
      echo "Cannot download Vault"
      exit 1
    fi
fi

if ! [ -f ${VAULT_ZIP} ]
then
    echo "Working in online mode and file '${VAULT_ZIP}' not found.. aborting"
    exit 1
fi

echo "Installing Vault to '${VAULT_INSTALL_HOME}'"
mkdir -p ${VAULT_CURRENT_HOME}
unzip ${VAULT_ZIP}
mv vault ${VAULT_CURRENT_HOME}/bin/
ln -s ${VAULT_CURRENT_HOME} current
rm -f ${VAULT_ZIP}

echo "Creating Vault data directory '${VAULT_DATA_DIR}'"
mkdir -p ${VAULT_DATA_DIR}

echo "Creating Vault configuration at '${VAULT_CURRENT}/conf'"
mkdir -p ${VAULT_CURRENT}/conf

echo "Creating Vault PID directory '${VAULT_PID_DIR}'"
mkdir -p ${VAULT_PID_DIR}


echo "Creating SSL config"
mkdir -p ${KYLO_SSL}
echo "Creating keystore and key password in ${VAULT_CONF}/password"
head -c 64 /dev/urandom | md5sum | cut -d' ' -f1 > ${VAULT_CONF}/password
PW=`cat ${VAULT_CONF}/password`

echo "Creating Kylo truststore password in ${KYLO_SSL}/password"
head -c 64 /dev/urandom | md5sum | cut -d' ' -f1 > ${KYLO_SSL}/password
KPW=`cat ${KYLO_SSL}/password`

echo "Creating keystore for root, ca, vault in ${VAULT_CONF}"
keytool -genkeypair -keystore ${VAULT_CONF}/root.jks -alias root -ext bc:c -validity 10000 -keyalg RSA -keysize 2048 -keypass ${PW} -storepass ${PW} -deststoretype pkcs12 -dname "cn=root"
keytool -genkeypair -keystore ${VAULT_CONF}/ca.jks -alias ca -ext bc:c -validity 10000 -keyalg RSA -keysize 2048 -keypass ${PW} -storepass ${PW} -deststoretype pkcs12  -dname "cn=CA"
keytool -genkeypair -keystore ${VAULT_CONF}/vault.jks -alias vault -validity 10000 -keyalg RSA -keysize 2048 -keypass ${PW} -storepass ${PW} -deststoretype pkcs12  -dname "cn=${VAULT_HOST}"
echo "Creating keystore for kylo in ${KYLO_SSL}"
keytool -genkeypair -keystore ${KYLO_KEYSTORE} -alias kylo -validity 10000 -keyalg RSA -keysize 2048 -keypass ${KPW} -storepass ${KPW} -deststoretype pkcs12  -dname "cn=${KYLO_HOST}"

echo "Storing root cert to ${VAULT_ROOT_CERT_PEM}"
keytool -keystore ${VAULT_CONF}/root.jks -alias root -exportcert -rfc -storepass ${PW} > ${VAULT_ROOT_CERT_PEM}

echo "Storing CA cert to ${VAULT_CA_CERT_PEM}"
keytool -storepass ${PW} -keystore ${VAULT_CONF}/ca.jks -certreq -alias ca | keytool -storepass ${PW} -keystore ${VAULT_CONF}/root.jks -gencert -alias root -ext BC=0 -rfc > ${VAULT_CA_CERT_PEM}
keytool -storepass ${PW} -keystore ${VAULT_CONF}/ca.jks -importcert -trustcacerts -noprompt -alias root -file ${VAULT_ROOT_CERT_PEM}
keytool -storepass ${PW} -keystore ${VAULT_CONF}/ca.jks -importcert -alias ca -file ${VAULT_CA_CERT_PEM}

echo "Storing Kylo cert to ${KYLO_CERT_PEM}"
keytool -storepass ${KPW} -keystore ${KYLO_KEYSTORE} -certreq -alias kylo | keytool -storepass ${PW} -keystore ${VAULT_CONF}/ca.jks -gencert -alias ca -ext ku:c=dig,keyEncipherment -rfc > ${KYLO_CERT_PEM}
keytool -storepass ${KPW} -keystore ${KYLO_KEYSTORE} -importcert -trustcacerts -noprompt -alias root -file ${VAULT_ROOT_CERT_PEM}
keytool -storepass ${KPW} -keystore ${KYLO_KEYSTORE} -importcert -alias ca -file ${VAULT_CA_CERT_PEM}
keytool -storepass ${KPW} -keystore ${KYLO_KEYSTORE} -importcert -alias kylo -file ${KYLO_CERT_PEM}
cp ${KYLO_CERT_PEM} ${VAULT_CONF}/kylo-cert.pem

echo "Storing Vault cert to ${VAULT_CERT_PEM}"
keytool -storepass ${PW} -keystore ${VAULT_CONF}/vault.jks -certreq -alias vault | keytool -storepass ${PW} -keystore ${VAULT_CONF}/ca.jks -gencert -alias ca -ext ku:c=dig,keyEncipherment -rfc > ${VAULT_CERT_PEM}

echo "Storing Vault key to ${VAULT_KEY_PEM}"
openssl pkcs12 -in ${VAULT_CONF}/vault.jks  -nodes -nocerts -out ${VAULT_KEY_PEM} -password pass:${PW}

echo "Creating Kylo truststore in ${KYLO_TRUSTSTORE}"
keytool -keystore ${KYLO_TRUSTSTORE} -storepass ${KPW} -importcert -trustcacerts -noprompt -alias root -file ${VAULT_ROOT_CERT_PEM}
keytool -keystore ${KYLO_TRUSTSTORE} -storepass ${KPW} -importcert -alias ca -file ${VAULT_CA_CERT_PEM}
keytool -keystore ${KYLO_TRUSTSTORE} -storepass ${KPW} -importcert -alias vault -file ${VAULT_CERT_PEM}

rm -f ${VAULT_CONF}/root.jks
rm -f ${VAULT_CONF}/ca.jks
rm -f ${VAULT_CONF}/vault.jks
rm -f ${VAULT_CONF}/password
rm -f ${VAULT_ROOT_CERT_PEM}
rm -f ${KYLO_CERT_PEM}
rm -f ${KYLO_SSL}/password

chown -R ${KYLO_USER}:${KYLO_GROUP} ${KYLO_SSL}
chmod 700 ${KYLO_SSL}
chmod 600 ${KYLO_SSL}/*



cat << EOF >> ${VAULT_CURRENT}/conf/kylo-policy.hcl
# This section grants all access on "secret/kylo/*"
path "secret/kylo/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
EOF

cat << EOF >> ${VAULT_CURRENT}/conf/vault.conf
backend "file" {
    path = "${VAULT_DATA_DIR}"
}

listener "tcp" {
  address = "0.0.0.0:${VAULT_PORT}"
  tls_cert_file = "${VAULT_CERT_PEM}"
  tls_key_file = "${VAULT_KEY_PEM}"

  tls_min_version = "tls10"
}

pid_file = "${VAULT_PID_FILE}"
disable_mlock = true
EOF

echo "Creating Vault log directory '${VAULT_LOG_DIR}'"
mkdir -p ${VAULT_LOG_DIR}
cat << EOF > ${VAULT_BIN_RUN}
#!/bin/bash

${VAULT_CURRENT_HOME}/bin/vault server \
            -config=${VAULT_CURRENT_HOME}/conf/vault.conf \
            >> ${VAULT_LOG_DIR}/vault.log 2>&1 &
EOF

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
          echo Running:
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

cat << EOF >> ${VAULT_BIN_INIT}
#! /bin/sh
export VAULT_ADDR=${VAULT_ADDRESS}
export VAULT_CAPATH="${VAULT_CA_CERT_PEM}"
${VAULT_CURRENT_HOME}/bin/vault init -key-shares=3 -key-threshold=3 | tee ${VAULT_CONF_INIT} > /dev/null
chmod 600 ${VAULT_CONF_INIT}
EOF

cat << EOF >> ${VAULT_BIN_UNSEAL}
#! /bin/sh
export VAULT_ADDR=${VAULT_ADDRESS}
export VAULT_CAPATH="${VAULT_CA_CERT_PEM}"
cat ${VAULT_CONF_INIT} | grep '^Unseal' | awk '{print \$4}' | for key in \$(cat -); do
    ${VAULT_CURRENT_HOME}/bin/vault unseal \${key} > ${VAULT_LOG_DIR}/unseal-vault.log 2>&1
done
EOF

cat << EOF >> ${VAULT_BIN_SETUP}
#! /bin/sh
export VAULT_TOKEN=\$1
export VAULT_ADDR=${VAULT_ADDRESS}
export VAULT_CAPATH="${VAULT_CA_CERT_PEM}"
#echo "Restoring non-versioned K/V backend at secret/"
# Non-versioned secrets only applicable to Vault v0.10.1+, cannot upgrade Vault to 0.10.1+ because
# upgrading Vault would mean upgrading Spring Vault, but cannot upgrade Spring Vault because Kylo is not running recent enough Spring
#${VAULT_CURRENT_HOME}/bin/vault secrets disable secret
#${VAULT_CURRENT_HOME}/bin/vault secrets enable -path secret -version 1 kv

echo "Setting up client cert authentication for Kylo"
${VAULT_CURRENT_HOME}/bin/vault auth-enable cert
${VAULT_CURRENT_HOME}/bin/vault write auth/cert/certs/kylo display_name=kylo policies=kylo certificate=@${VAULT_CONF}/kylo-cert.pem ttl=3600
${VAULT_CURRENT_HOME}/bin/vault policy-write kylo ${VAULT_CONF}/kylo-policy.hcl

rm -f ${VAULT_CONF}/kylo-policy.hcl
rm -f ${VAULT_CONF}/kylo-cert.pem

EOF


echo "Assigning owner and group to '$VAULT_USER:$VAULT_GROUP'"
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_INSTALL_HOME}
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_PID_DIR}
chown -R ${VAULT_USER}:${VAULT_GROUP} ${VAULT_LOG_DIR}
chmod 700 -R ${VAULT_PID_DIR}
chmod 700 -R ${VAULT_LOG_DIR}
chmod 700 ${VAULT_SERVICE_FILE}
chmod 700 -R ${VAULT_INSTALL_HOME}
chmod 600 ${VAULT_CURRENT_HOME}/conf/*
chmod 700 -R ${VAULT_DATA_DIR}

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

echo "Updating Kylo configuration in ${PROPS}"
sed -i -r "s|^vault\.keyStorePassword=.*|vault\.keyStorePassword=${KPW}|" ${PROPS}
sed -i -r "s|^vault\.keyStoreDirectory=.*|vault\.keyStoreDirectory=${KYLO_SSL}|" ${PROPS}
sed -i -r "s|^vault\.trustStorePassword=.*|vault\.trustStorePassword=${KPW}|" ${PROPS}
sed -i -r "s|^vault\.trustStoreDirectory=.*|vault\.trustStoreDirectory=${KYLO_SSL}|" ${PROPS}
echo "Copying Vault plugin to Kylo"
cp ${KYLO_HOME}/setup/plugins/kylo-catalog-credential-vault-*.jar ${KYLO_HOME}/kylo-services/plugin/
chown ${KYLO_USER}:${KYLO_GROUP} ${KYLO_HOME}/kylo-services/plugin/kylo-catalog-credential-vault-*.jar

echo "Vault installation complete"
echo "The unseal keys and root token have been stored in "${VAULT_CONF_INIT}"."
