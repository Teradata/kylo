Spark Shell Service
===================

A REST API for transforming SQL tables in real-time. When creating a
feed Kylo, a user would be able to see a live
preview of how their data would look when stored in its final
destination. Any transformations would be translated into Scala which
could then be used in production to apply the same transformations.

Known Issues
------------

__A NoClassDefFoundError may be thrown on CDH 5.7__  
Add the following line to the bottom of `/etc/spark/conf/spark-env.sh`:  
`HADOOP_CONF_DIR="$HADOOP_CONF_DIR:/etc/hive/conf"`

__An IOException may be thrown if the Hive scratch directory is not writable__  
The `/tmp/kylo` directory should be owned by the `kylo` user and have the permissions 0700.
