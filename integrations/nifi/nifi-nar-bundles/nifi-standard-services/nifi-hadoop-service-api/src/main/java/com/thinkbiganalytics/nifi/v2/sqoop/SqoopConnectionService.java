package com.thinkbiganalytics.nifi.v2.sqoop;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 * @author jagrut sharma
 */
@Tags({"thinkbig", "ingest", "sqoop", "rdbms", "database", "table"})
@CapabilityDescription("Provides connection configuration for running a sqoop job")

/*
Interface for service to get connection details for sqoop
 */
public interface SqoopConnectionService extends ControllerService {
    /* Driver to access source system */
    String getDriver();

    /* Connection string to access source system */
    String getConnectionString();

    /* User name for source system authentication */
    String getUserName();

    /* Location of encrypted password file on HDFS */
    String getPasswordHdfsFile();

    /* Passphrase to decrypt password */
    String getPasswordPassphrase();
}
