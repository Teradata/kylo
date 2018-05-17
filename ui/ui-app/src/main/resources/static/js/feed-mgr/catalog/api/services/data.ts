// TODO file for testing

import {DataSource} from "../models/datasource";

export const dataSources: DataSource[] = [
    {
        id: "1",
        title: "Gregs Amazon S3",
        connector: {
            id: "2",
            title: "Amazon S3",
            icon: "amazon",
            tabs: [{label: "Files", sref: ".browse"}],
            template: {
                options: {
                    "spark.hadoop.fs.s3n.awsAccessKeyId": "",
                    "spark.hadoop.fs.s3n.awsSecretAccessKey": ""
                },
                paths: ["s3n://"]
            }
        }
    },
    {
        id: "2",
        title: "Ruslans Amazon S3",
        connector: {
            id: "2",
            title: "Amazon S3",
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
    },
    {
        id: "3",
        title: "Localhost:9092",
        connector: {
            id: "3",
            title: "Kafka",
            icon: "kafka",
            template: {
                format: "org.apache.spark.sql.kafka010.KafkaSourceProvider",
                jars: ["file:/opt/nifi/mysql/kafka-clients-0.10.0.1.jar", "file:/opt/nifi/mysql/spark-sql-kafka-0-10_2.11-2.2.0.jar"],
                options: {
                    "kafka.bootstrap.servers": "localhost:9092"
                }
            }
        },
    },
    {
        id: "4",
        title: "Localhost:9093",
        connector: {
            id: "3",
            title: "Kafka",
            icon: "kafka",
            template: {
                format: "org.apache.spark.sql.kafka010.KafkaSourceProvider",
                jars: ["file:/opt/nifi/mysql/kafka-clients-0.10.0.1.jar", "file:/opt/nifi/mysql/spark-sql-kafka-0-10_2.11-2.2.0.jar"],
                options: {
                    "kafka.bootstrap.servers": "localhost:9093"
                }
            }
        },
    },
    {
        id: "5",
        title: "File Upload /var/dropzone",
        connector: {
            id: "4",
            title: "File Upload",
            icon: "file_upload",
            tabs: [{label: "Files", sref: ".upload"}]
        }
    },
    {
        id: "6",
        title: "File Upload /opt/kylo/kylo-ui/config",
        connector: {
            id: "4",
            title: "File Upload",
            icon: "file_upload",
            tabs: [{label: "Files", sref: ".upload"}]
        }
    },
    {
        id: "7",
        title: "Ruslans HDFS home",
        connector: {
            id: "5",
            title: "HDFS",
            icon: "hadoop",
            tabs: [{label: "Files", sref: ".browse"}],
            template: {
                paths: ["hdfs://users/ruslans"]
            }
        },
    },
    {
        id: "8",
        title: "Spark logs",
        connector: {
            id: "5",
            title: "HDFS",
            icon: "hadoop",
            tabs: [{label: "Files", sref: ".browse"}],
            template: {
                paths: ["hdfs://users/spark/logs"]
            }
        },
    },
    {
        id: "9",
        title: "Teradata Database 1",
        connector: {
            id: "6",
            title: "Teradata",
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
    },
    {
        id: "10",
        title: "Teradata Database 2",
        connector: {
            id: "6",
            title: "Teradata",
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
    },
    {
        id: "11",
        title: "Greg's google storage",
        connector: {
            id: "7",
            title: "Google Cloud Storage",
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
        },
    }
];

