#!/bin/bash
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-spark-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-spark-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-core-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-core-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-core-service-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-core-service-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-elasticsearch-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-elasticsearch-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-hadoop-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-hadoop-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-hadoop-service-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-hadoop-service-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-provenance-repo-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-provenance-repo-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-standard-services-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-standard-services-nar.nar

ln -f -s /opt/nifi/data/lib/thinkbig-nifi-sentry-authorization-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-sentry-authorization-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-ranger-authorization-service-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-ranger-authorization-service-nar.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-ranger-authorization-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-ranger-authorization-nar-0.4.0-SNAPSHOT.nar
ln -f -s /opt/nifi/data/lib/thinkbig-nifi-ranger-authorization-service-api-nar-*.nar /opt/nifi/current/lib/thinkbig-nifi-ranger-authorization-service-api-nar.nar

ln -f -s /opt/nifi/data/lib/app/thinkbig-spark-interpreter-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/thinkbig-spark-interpreter-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/thinkbig-spark-validate-cleanse-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/thinkbig-spark-validate-cleanse-jar-with-dependencies.jar
ln -f -s /opt/nifi/data/lib/app/thinkbig-spark-job-profiler-*-jar-with-dependencies.jar /opt/nifi/current/lib/app/thinkbig-spark-job-profiler-jar-with-dependencies.jar