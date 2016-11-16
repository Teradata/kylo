package com.thinkbiganalytics.nifi.v2.sqoop;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 * Interface for a connection provider service for Sqoop<br>
 *     Provides information for connecting to a relational system
 * @author jagrut sharma
 */
@Tags({"thinkbig", "ingest", "sqoop", "rdbms", "database", "table"})
@CapabilityDescription("Provides information for connecting to a relational system for running a sqoop job")

public interface SqoopConnectionService extends ControllerService {
    /**
     * Driver to access source system
     */
    String getDriver();

    /**
     * Connection string to access source system
     */
    String getConnectionString();

    /**
     * User name for source system authentication
     */
    String getUserName();

    /**
     * Get password mode
     */
    PasswordMode getPasswordMode();

    /**
     * Location of encrypted password file on HDFS
     */
    String getPasswordHdfsFile();

    /**
     * Passphrase to decrypt password
     */
    String getPasswordPassphrase();

    /**
     * Get the password
     */
    String getEnteredPassword();

}

