// TODO file for testing

import {Connector} from "../models/connector";

export const connectors: Connector[] = [
    {
        id: "1",
        title: "Gregs Amazon S3",
        type: "s3",
        icon: "amazon",
        tabs: [{label: "Files", sref: ".browse"}],
        template: {
            options: {
                "spark.hadoop.fs.s3n.awsAccessKeyId": "",
                "spark.hadoop.fs.s3n.awsSecretAccessKey": ""
            },
            paths: ["s3n://"]
        }
    },
    {
        id: "2",
        title: "Ruslans Amazon S3",
        type: "s3",
        icon: "amazon",
        tabs: [{label: "Files", sref: ".browse"}],
        template: {
            options: {
                "spark.hadoop.fs.s3n.awsAccessKeyId": "",
                "spark.hadoop.fs.s3n.awsSecretAccessKey": ""
            },
            paths: ["s3n://"]
        }
    },
    {
        id: "3",
        title: "Localhost:9092",
        type: "kafka",
        icon: "kafka",
        template: {
            format: "org.apache.spark.sql.kafka010.KafkaSourceProvider",
            jars: ["file:/opt/nifi/mysql/kafka-clients-0.10.0.1.jar", "file:/opt/nifi/mysql/spark-sql-kafka-0-10_2.11-2.2.0.jar"],
            options: {
                "kafka.bootstrap.servers": "localhost:9092"
            }
        }
    },
    {
        id: "4",
        title: "Localhost:9093",
        type: "kafka",
        icon: "kafka",
        template: {
            format: "org.apache.spark.sql.kafka010.KafkaSourceProvider",
            jars: ["file:/opt/nifi/mysql/kafka-clients-0.10.0.1.jar", "file:/opt/nifi/mysql/spark-sql-kafka-0-10_2.11-2.2.0.jar"],
            options: {
                "kafka.bootstrap.servers": "localhost:9093"
            }
        }
    },
    {
        id: "5",
        title: "File Upload /var/dropzone",
        type: "file-upload",
        icon: "file_upload",
        tabs: [{label: "Files", sref: ".upload"}]
    },
    {
        id: "6",
        title: "File Upload /opt/kylo/kylo-ui/config",
        type: "file-upload",
        icon: "file_upload",
        tabs: [{label: "Files", sref: ".upload"}]
    },
    {
        id: "7",
        title: "Ruslans HDFS home",
        type: "hdfs",
        icon: "hadoop",
        tabs: [{label: "Files", sref: ".browse"}],
        template: {
            paths: ["hdfs://users/ruslans"]
        }
    },
    {
        id: "8",
        title: "Spark logs",
        type: "hdfs",
        icon: "hadoop",
        tabs: [{label: "Files", sref: ".browse"}],
        template: {
            paths: ["hdfs://users/spark/logs"]
        }
    },
    {
        id: "9",
        title: "Teradata Database 1",
        type: "jdbc",
        color: "orange-700",
        tabs: [{label: "Connection", sref: ".connection"}],
        template: {
            format: "jdbc",
            jars: ["file:/opt/nifi/mysql/tdgssconfig.jar;file:/opt/nifi/mysql/terajdbc4.jar"],
            options: {
                "driver": "com.teradata.jdbc.TeraDriver",
                "url": "jdbc:teradata://1.2.3.4/database1",
                "user": "user-id",
                "password": ""
            }
        }
    },
    {
        id: "10",
        title: "Teradata Database 2",
        type: "jdbc",
        color: "orange-700",
        tabs: [{label: "Connection", sref: ".connection"}],
        template: {
            format: "jdbc",
            jars: ["file:/opt/nifi/mysql/tdgssconfig.jar;file:/opt/nifi/mysql/terajdbc4.jar"],
            options: {
                "driver": "com.teradata.jdbc.TeraDriver",
                "url": "jdbc:teradata://1.2.3.4/database2",
                "user": "user-id",
                "password": ""
            }
        }
    },
    {
        id: "11",
        title: "Greg's google storage",
        type: "gcs",
        icon: "google",
        tabs: [{label: "Files", sref: ".browse"}],
        template: {
            jars: ["file:/opt/nifi/mysql/gcs-connector-latest-hadoop2.jar"],
            options: {
                "spark.hadoop.google.cloud.auth.service.account.email": "drive@user-id.iam.gserviceaccount.com",
                "spark.hadoop.google.cloud.auth.service.account.keyfile": "/opt/nifi/mysql/private-key.p12"
            },
            paths: ["gcs://some/path"]
        }
    }
];
