#!/bin/bash

ln -f -s /opt/nifi/data/lib/thinkbig-nifi-core-service-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-core-service-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-standard-services-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-standard-services-nar.nar

ln -f -s /opt/nifi/data/lib/app/thinkbig-spark-interpreter-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/thinkbig-spark-interpreter-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/thinkbig-spark-validate-cleanse-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/thinkbig-spark-validate-cleanse-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/thinkbig-spark-job-profiler-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/thinkbig-spark-job-profiler-jar-with-dependencies.jar

if grep -q "nifi.version=0." /opt/nifi/current/conf/nifi.properties; then
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-core-v0-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-core-v0-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-spark-v0-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-spark-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-hadoop-v0-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-hadoop-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-hadoop-service-v0-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-hadoop-service-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-provenance-repo-v0-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-provenance-repo-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-elasticsearch-v0-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-elasticsearch-v0-nar.nar
else
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-core-v1-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-core-v1-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-spark-v1-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-spark-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-hadoop-v1-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-hadoop-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-hadoop-service-v1-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-hadoop-service-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-provenance-repo-v1-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-provenance-repo-nar.nar
    ln -f -s /opt/nifi/data/lib/thinkbig-nifi-elasticsearch-v1-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-elasticsearch-v1-nar.nar
fi
