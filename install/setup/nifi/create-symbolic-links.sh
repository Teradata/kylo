#!/bin/bash

ln -f -s /opt/nifi/data/lib/kylo-nifi-core-service-nar-*.nar /opt/nifi/current/lib/kylo-nifi-core-service-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-standard-services-nar-*.nar /opt/nifi/current/lib/kylo-nifi-standard-services-nar.nar

ln -f -s /opt/nifi/data/lib/kylo-nifi-core-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-core-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-spark-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-spark-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-spark-service-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-spark-service-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-hadoop-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-hadoop-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-hadoop-service-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-hadoop-service-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-provenance-repo-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-provenance-repo-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-elasticsearch-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-elasticsearch-nar.nar

if [ -z ${SPARK_PROFILE} ]; then
        SPARK_SUBMIT=$(which spark-submit)
        if [ -z ${SPARK_SUBMIT} ]; then
                >&2 echo "ERROR: spark-submit not on path.  Has spark been installed?"
                exit 1
        fi
        if ! [ -x ${SPARK_SUBMIT} ]; then
                >&2 echo "ERROR: spark-submit found but not suitable for execution.  Has spark been installed?"
                exit 1
        fi
        SPARK_PROFILE="spark-v"$(spark-submit --version 2>&1 | grep -o "version [0-9]" | grep -o "[0-9]" | head -1)
else
        if ! [[ $SPARK_PROFILE =~ spark-v[0-9] ]]; then
                >&2 echo "ERROR: variable SPARK_PROFILE not usable, expected it to be like spark-v1 or spark-v2 but found '$SPARK_PROFILE'"
                exit 1
        fi
fi

ln -f -s /opt/nifi/data/lib/app/kylo-spark-validate-cleanse-${SPARK_PROFILE}-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/kylo-spark-validate-cleanse-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/kylo-spark-job-profiler-${SPARK_PROFILE}-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/kylo-spark-job-profiler-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/kylo-spark-interpreter-${SPARK_PROFILE}-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/kylo-spark-interpreter-jar-with-dependencies.jar

chown -h nifi:users /opt/nifi/current/lib/kylo*.nar
chown -h nifi:users /opt/nifi/current/lib/app/kylo*.jar
