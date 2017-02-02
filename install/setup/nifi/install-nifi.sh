#!/bin/bash
NIFI_INSTALL_HOME=/opt/nifi
NIFI_DATA=$NIFI_INSTALL_HOME/data
NIFI_VERSION=1.0.0

offline=false
working_dir=$2

if [ $# > 1 ]
then
    if [ "$1" = "-o" ] || [ "$1" = "-O" ]
    then
        echo "Working in offline mode"
        offline=true
    fi
fi

echo "Installing NiFI"
echo "Creating a new nifi user"
mkdir $NIFI_INSTALL_HOME
cd $NIFI_INSTALL_HOME

if [ $offline = true ]
then
    cp $working_dir/nifi/nifi-${NIFI_VERSION}-bin.tar.gz .
else
    echo "Download nifi distro and install"
    curl -O https://archive.apache.org/dist/nifi/${NIFI_VERSION}/nifi-${NIFI_VERSION}-bin.tar.gz
fi

if ! [ -f nifi-${NIFI_VERSION}-bin.tar.gz ]
then
    echo "Working in online mode and file not found.. exiting"
    exit 1
fi

tar -xvf nifi-${NIFI_VERSION}-bin.tar.gz
rm -f current
ln -s nifi-${NIFI_VERSION} current

echo "Externalizing NiFi data files and folders to support upgrades"
mkdir -p $NIFI_DATA/conf
mv $NIFI_INSTALL_HOME/current/conf/authorizers.xml $NIFI_DATA/conf
mv $NIFI_INSTALL_HOME/current/conf/login-identity-providers.xml $NIFI_DATA/conf


echo "Changing permissions to the nifi user"
chown -R nifi:nifi /opt/nifi
echo "NiFi installation complete"

echo "Modifying the nifi.properties file"
sed -i 's/nifi.flow.configuration.file=.\/conf\/flow.xml.gz/nifi.flow.configuration.file=\/opt\/nifi\/data\/conf\/flow.xml.gz/' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.flow.configuration.archive.dir=.\/conf\/archive\//nifi.flow.configuration.archive.dir=\/opt\/nifi\/data\/conf\/archive\//' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.authorizer.configuration.file=.\/conf\/authorizers.xml/nifi.authorizer.configuration.file=\/opt\/nifi\/data\/conf\/authorizers.xml/' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.templates.directory=.\/conf\/templates/nifi.templates.directory=\/opt\/nifi\/data\/conf\/templates/' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.flowfile.repository.directory=.\/flowfile_repository/nifi.flowfile.repository.directory=\/opt\/nifi\/data\/flowfile_repository/' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.content.repository.directory.default=.\/content_repository/nifi.content.repository.directory.default=\/opt\/nifi\/data\/content_repository/' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.content.repository.archive.enabled=true/nifi.content.repository.archive.enabled=false/' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.provenance.repository.directory.default=.\/provenance_repository/nifi.provenance.repository.directory.default=\/opt\/nifi\/data\/provenance_repository/' $NIFI_INSTALL_HOME/current/conf/nifi.properties
sed -i 's/nifi.web.http.port=8080/nifi.web.http.port=8079/' $NIFI_INSTALL_HOME/current/conf/nifi.properties

echo "Updating the log file path"
sed -i 's/NIFI_LOG_DIR=\".*\"/NIFI_LOG_DIR=\"\/var\/log\/nifi\"/' $NIFI_INSTALL_HOME/current/bin/nifi-env.sh
