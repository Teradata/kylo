#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# LIVY ENVIRONMENT VARIABLES
#
# - JAVA_HOME       Java runtime to use. By default use "java" from PATH.
# - HADOOP_CONF_DIR Directory containing the Hadoop / YARN configuration to use.
# - SPARK_HOME      Spark which you would like to use in Livy.
# - SPARK_CONF_DIR  Optional directory where the Spark configuration lives.
#                   (Default: $SPARK_HOME/conf)
# - LIVY_LOG_DIR    Where log files are stored. (Default: ${LIVY_HOME}/logs)
# - LIVY_PID_DIR    Where the pid file is stored. (Default: /tmp)
# - LIVY_SERVER_JAVA_OPTS  Java Opts for running livy server (You can set jvm related setting here,
#                          like jvm memory/gc algorithm and etc.)
# - LIVY_IDENT_STRING A name that identifies the Livy server instance, used to generate log file
#                     names. (Default: name of the user starting Livy).
# - LIVY_MAX_LOG_FILES Max number of log file to keep in the log directory. (Default: 5.)
# - LIVY_NICENESS   Niceness of the Livy server process when running in the background. (Default: 0.)

export SPARK_MAJOR_VERSION=1
export SPARK_HOME=/usr/hdp/current/spark-client
export SPARK_CONF_DIR=/etc/spark/conf
