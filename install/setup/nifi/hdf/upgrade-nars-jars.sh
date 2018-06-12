#!/bin/bash

#######
# Note: This script has not been tested yet but should be close. Please create a Jira if you use it and there is a typo somewhere
######

if [ $# -eq 4 ]
then
    NIFI_KYLO_FOLDER=$1
    HDF_NIFI_HOME_FOLDER=$2
    NIFI_USER=$3
    NIFI_GROUP=$4
else
    echo "Unknown arguments. You must pass in the nifi-kylo setup folder location, the HDF NiFI Home folder location, and user:group names. For example: /opt/nifi-kylo /usr/hdf/current/nifi nifi:nifi"
    exit 1
fi

rm -f $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi*.nar
rm -f $HDF_NIFI_HOME_FOLDER/lib/app/kylo-spark*.jar

echo "Creating symbolic links"

# Expected path something like /usr/hdf/current/nifi
framework_name=$(find $HDF_NIFI_HOME_FOLDER/lib/ -name "nifi-framework-api*.jar")
prefix="$HDF_NIFI_HOME_FOLDER/lib/nifi-framework-api-"
len=${#prefix}
ver=${framework_name:$len}

ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-elasticsearch-v1-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-elasticsearch-nar.nar

if [[ $ver == 1.0* ]] || [[ $ver == 1.1* ]] ;
then

echo "Creating symlinks for NiFi version $ver compatible nars"

ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-core-service-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-core-service-nar.nar
ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-standard-services-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-standard-services-nar.nar
ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-core-v1-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-core-nar.nar
ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-spark-v1-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-spark-nar.nar
ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-hadoop-v1-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-hadoop-nar.nar
ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-hadoop-service-v1-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-hadoop-service-nar.nar
ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-provenance-repo-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-provenance-repo-nar.nar
ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-core-service-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-core-service-nar.nar

elif  [[ $ver == 1.2* ]] || [[ $ver == 1.3* ]] || [[ $ver == 1.4* ]] ;
then
   echo "Creating symlinks for NiFi version $ver compatible nars"
  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-provenance-repo-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-provenance-repo-nar.nar

  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-core-service-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-core-service-nar.nar
  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-standard-services-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-standard-services-nar.nar
  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-core-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-core-nar.nar
  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-spark-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-spark-nar.nar
  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-spark-service-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-spark-service-nar.nar
  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-hadoop-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-hadoop-nar.nar
  ln -f -s $NIFI_KYLO_FOLDER/lib/kylo-nifi-hadoop-service-v1.2-nar-*.nar $HDF_NIFI_HOME_FOLDER/lib/kylo-nifi-hadoop-service-nar.nar

fi

ln -f -s $NIFI_KYLO_FOLDER/lib/app/kylo-spark-validate-cleanse-spark-v2-*-jar-with-dependencies.jar $HDF_NIFI_HOME_FOLDER/lib/app/kylo-spark-validate-cleanse-jar-with-dependencies.jar
ln -f -s $NIFI_KYLO_FOLDER/lib/app/kylo-spark-job-profiler-spark-v2-*-jar-with-dependencies.jar $HDF_NIFI_HOME_FOLDER/lib/app/kylo-spark-job-profiler-jar-with-dependencies.jar
ln -f -s $NIFI_KYLO_FOLDER/lib/app/kylo-spark-interpreter-spark-v2-*-jar-with-dependencies.jar $HDF_NIFI_HOME_FOLDER/lib/app/kylo-spark-interpreter-jar-with-dependencies.jar
ln -f -s $NIFI_KYLO_FOLDER/lib/app/kylo-spark-multi-exec-spark-v2-*-jar-with-dependencies.jar $HDF_NIFI_HOME_FOLDER/lib/app/kylo-spark-multi-exec-jar-with-dependencies.jar


echo "Updating permissions for the nifi sym links"
chown -h nifi:nifi $HDF_NIFI_HOME_FOLDER/lib/kylo*.nar
chown -h nifi:nifi $HDF_NIFI_HOME_FOLDER/lib/app/kylo*.jar


echo "Update complete"

