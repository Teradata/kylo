#!/bin/bash
NIFI_VERSION=$1
NIFI_INSTALL_HOME=$2
NIFI_USER=$3
NIFI_GROUP=$4
WORKING_DIR=$5
NIFI_DATA=$NIFI_INSTALL_HOME/data

# If we copy the NiFi tarball from $WORKING_DIR/nifi/ instead of downloading it
offline=false

if [ "$6" = "-o" ] || [ "$6" = "-O" ]
then
    echo "Working in offline mode"
    offline=true
fi

if [ $# -lt 3 ] || [ $# -gt 6 ]; then
    echo "Unknown arguments. Arg1 should be the nifi version, Arg2 should be the nifi home, Arg3 should be the nifi user, Arg4 should be the nifi group. For offline mode pass Arg5 the kylo setup folder and Arg6 the -o -or -O option"
    exit 1
fi

echo "The NIFI home folder is $NIFI_INSTALL_HOME using permissions $NIFI_USER:$NIFI_GROUP"

echo "Installing NiFI"
mkdir $NIFI_INSTALL_HOME
cd $NIFI_INSTALL_HOME

if [ $offline = true ]
then
    cp $WORKING_DIR/nifi/nifi-${NIFI_VERSION}-bin.tar.gz .
else
    echo "Downloading NiFi ${NIFI_VERSION} distro"
    curl -f -O -k https://archive.apache.org/dist/nifi/${NIFI_VERSION}/nifi-${NIFI_VERSION}-bin.tar.gz
fi

if ! [ -f nifi-${NIFI_VERSION}-bin.tar.gz ]
then
    echo "Working in online mode and file not found.. exiting"
    exit 1
fi

echo "Installing NiFi"
tar -xvf nifi-${NIFI_VERSION}-bin.tar.gz
rm -f nifi-${NIFI_VERSION}-bin.tar.gz
ln -s nifi-${NIFI_VERSION} current

echo "Externalizing NiFi data files and folders to support upgrades"
mkdir -p $NIFI_DATA/conf
mv $NIFI_INSTALL_HOME/current/conf/authorizers.xml $NIFI_DATA/conf
mv $NIFI_INSTALL_HOME/current/conf/login-identity-providers.xml $NIFI_DATA/conf


echo "Changing permissions to the nifi user"
chown -R $NIFI_USER:$NIFI_GROUP $NIFI_INSTALL_HOME
echo "NiFi installation complete"

echo "Modifying the nifi.properties file"
sed -i "s|nifi.flow.configuration.file=.\/conf\/flow.xml.gz|nifi.flow.configuration.file=$NIFI_INSTALL_HOME\/data\/conf\/flow.xml.gz|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.flow.configuration.archive.dir=.\/conf\/archive\/|nifi.flow.configuration.archive.dir=$NIFI_INSTALL_HOME\/data\/conf\/archive\/|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.authorizer.configuration.file=.\/conf\/authorizers.xml|nifi.authorizer.configuration.file=$NIFI_INSTALL_HOME\/data\/conf\/authorizers.xml|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.templates.directory=.\/conf\/templates|nifi.templates.directory=$NIFI_INSTALL_HOME\/data\/conf\/templates|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.flowfile.repository.directory=.\/flowfile_repository|nifi.flowfile.repository.directory=$NIFI_INSTALL_HOME\/data\/flowfile_repository|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.content.repository.directory.default=.\/content_repository|nifi.content.repository.directory.default=$NIFI_INSTALL_HOME\/data\/content_repository|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.content.repository.archive.enabled=true|nifi.content.repository.archive.enabled=false|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.provenance.repository.directory.default=.\/provenance_repository|nifi.provenance.repository.directory.default=$NIFI_INSTALL_HOME\/data\/provenance_repository|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.web.http.port=8080|nifi.web.http.port=8079|" $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i "s|nifi.provenance.repository.implementation=org.apache.nifi.provenance.PersistentProvenanceRepository|nifi.provenance.repository.implementation=com.thinkbiganalytics.nifi.provenance.repo.KyloPersistentProvenanceEventRepository|" $NIFI_INSTALL_HOME/current/conf/nifi.properties

# sed -i "s|=/opt/nifi|=/apps/nifi|" /apps/nifi/current/conf/nifi.properties

echo "Updating the log file path"
sed -i 's/NIFI_LOG_DIR=\".*\"/NIFI_LOG_DIR=\"\/var\/log\/nifi\"/' $NIFI_INSTALL_HOME/current/bin/nifi-env.sh
