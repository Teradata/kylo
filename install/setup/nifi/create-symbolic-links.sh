#!/bin/bash

ln -f -s /opt/nifi/data/lib/kylo-nifi-core-service-nar-*.nar /opt/nifi/current/lib/kylo-nifi-core-service-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-standard-services-nar-*.nar /opt/nifi/current/lib/kylo-nifi-standard-services-nar.nar

ln -f -s /opt/nifi/data/lib/kylo-nifi-core-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-core-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-spark-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-spark-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-hadoop-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-hadoop-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-hadoop-service-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-hadoop-service-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-provenance-repo-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-provenance-repo-nar.nar
ln -f -s /opt/nifi/data/lib/kylo-nifi-elasticsearch-v1-nar-*.nar /opt/nifi/current/lib/kylo-nifi-elasticsearch-nar.nar

SPARK_PROFILE="spark-v"$(spark-submit --version 2>&1 | grep -o "version [0-9]" | grep -o "[0-9]" | head -1)
ln -f -s /opt/nifi/data/lib/app/kylo-spark-validate-cleanse-${SPARK_PROFILE}-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/kylo-spark-validate-cleanse-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/kylo-spark-job-profiler-${SPARK_PROFILE}-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/kylo-spark-job-profiler-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/kylo-spark-interpreter-${SPARK_PROFILE}-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/kylo-spark-interpreter-jar-with-dependencies.jar

chown -h nifi:users /opt/nifi/current/lib/kylo*.nar
chown -h nifi:users /opt/nifi/current/lib/app/kylo*.jar
