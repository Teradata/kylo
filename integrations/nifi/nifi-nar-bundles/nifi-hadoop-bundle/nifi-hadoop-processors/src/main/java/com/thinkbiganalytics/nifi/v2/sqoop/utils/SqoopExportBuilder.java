package com.thinkbiganalytics.nifi.v2.sqoop.utils;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.v2.sqoop.PasswordMode;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.ExportNullInterpretationStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.security.DecryptPassword;

import org.apache.nifi.logging.ComponentLog;

/**
 * A class to build a sqoop export command that can be run on the command line
 */
public class SqoopExportBuilder {

    private final static String SPACE_STRING = " ";
    private final static String START_SPACE_QUOTE = " \"";
    private final static String END_QUOTE_SPACE = "\" ";
    private final static String QUOTE = "\"";
    private final static String EQUAL_STRING = "=";
    private final static String MASK_STRING = "*****";
    private final static String UNABLE_TO_DECRYPT_STRING = "UNABLE_TO_DECRYPT_ENCRYPTED_PASSWORD";
    private final static String targetPasswordLoaderClassLabel = "-Dorg.apache.sqoop.credentials.loader.class";
    private final static String targetPasswordLoaderClassValue = "org.apache.sqoop.util.password.CryptoFileLoader";
    private final static String targetPasswordPassphraseLabel = "-Dorg.apache.sqoop.credentials.loader.crypto.passphrase";
    private final static String targetConnectionStringLabel = "--connect";
    private final static String targetUserNameLabel = "--username";
    private final static String targetPasswordHdfsFileLabel = "--password-file";
    private final static String targetPasswordClearTextLabel = "--password";
    private final static String targetConnectionManagerLabel = "--connection-manager";
    private final static String targetDriverLabel = "--driver";
    private final static String targetTableNameLabel = "--table";
    private final static String sourceHdfsDirectoryLabel = "--export-dir";
    private final static String sourceHdfsFileDelimiterLabel = "--input-fields-terminated-by";
    private final static String sourceNullInterpretationStrategyCustomNullStringLabel = "--input-null-string";
    private final static String sourceNullInterpretationStrategyCustomNullNonStringLabel = "--input-null-non-string";
    private final static String sourceNullInterpretationStrategyHiveDefaultNullStringLabelAndValue = "--input-null-string '\\\\N'";
    private final static String sourceNullInterpretationStrategyHiveDefaultNullNonStringLabelAndValue = "--input-null-non-string '\\\\N'";
    private final static String clusterMapTasksLabel = "--num-mappers";
    private final static String operationName = "sqoop";
    private final static String operationType = "export";
    private final Integer DEFAULT_CLUSTER_MAP_TASKS = 4;
    private String targetPasswordPassphrase;
    private String targetConnectionString;
    private String targetUserName;
    private PasswordMode passwordMode;
    private String targetPasswordHdfsFile;
    private String targetEnteredPassword;
    private String targetConnectionManager;
    private String targetDriver;
    private String targetTableName;
    private String sourceHdfsDirectory;
    private String sourceHdfsFileDelimiter;
    private ExportNullInterpretationStrategy sourceNullInterpretationStrategy = ExportNullInterpretationStrategy.HIVE_DEFAULT;
    private String sourceNullInterpretationStrategyCustomNullString;
    private String sourceNullInterpretationStrategyCustomNullNonString;
    private Integer clusterMapTasks = DEFAULT_CLUSTER_MAP_TASKS;
    private ComponentLog logger = null;

    /**
     * Set logger
     *
     * @param logger Logger
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setLogger(ComponentLog logger) {
        this.logger = logger;
        logger.info("Logger set to: {}", new Object[]{this.logger});
        return this;
    }

    /**
     * Set connection string for target system
     *
     * @param targetConnectionString target connection string
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetConnectionString(String targetConnectionString) {
        this.targetConnectionString = targetConnectionString;
        if (logger != null) {
            logger.info("Target Connection String set to: {}", new Object[]{this.targetConnectionString});
        }
        return this;
    }

    /**
     * Set user name for connecting to target system
     *
     * @param targetUserName user name
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
        if (logger != null) {
            logger.info("Target User Name set to: {}", new Object[]{this.targetUserName});
        }
        return this;
    }

    /**
     * Set password mode for providing password to connect to target system
     *
     * @param passwordMode {@link PasswordMode}
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setPasswordMode(PasswordMode passwordMode) {
        this.passwordMode = passwordMode;
        if (logger != null) {
            logger.info("Target Password Mode set to: {}", new Object[]{this.passwordMode});
        }
        return this;
    }

    /**
     * Set location of password file on HDFS
     *
     * @param targetPasswordHdfsFile location on HDFS
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetPasswordHdfsFile(String targetPasswordHdfsFile) {
        this.targetPasswordHdfsFile = targetPasswordHdfsFile;
        if (logger != null) {
            logger.info("Target Password File (HDFS) set to: {}", new Object[]{MASK_STRING});
        }
        return this;
    }

    /**
     * Set passphrase used to generate encrypted password
     *
     * @param targetPasswordPassphrase passphrase
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetPasswordPassphrase(String targetPasswordPassphrase) {
        this.targetPasswordPassphrase = targetPasswordPassphrase;
        if (logger != null) {
            logger.info("Target Password Passphrase set to: {}", new Object[]{MASK_STRING});
        }
        return this;
    }

    /**
     * Set password entered (clear text / encrypted)
     *
     * @param targetEnteredPassword password string
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetEnteredPassword(String targetEnteredPassword) {
        this.targetEnteredPassword = targetEnteredPassword;
        if (logger != null) {
            logger.info("Target Entered Password set to: {}", new Object[]{MASK_STRING});
        }
        return this;
    }

    /**
     * Set Connection Manager class for target system
     *
     * @param targetConnectionManager connection manager class
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetConnectionManager(String targetConnectionManager) {
        this.targetConnectionManager = targetConnectionManager;
        if (logger != null) {
            logger.info("Target Connection Manager set to: {}", new Object[]{this.targetConnectionManager});
        }
        return this;
    }

    /**
     * Set JDBC driver for target system
     *
     * @param targetDriver target driver
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetDriver(String targetDriver) {
        this.targetDriver = targetDriver;
        if (logger != null) {
            logger.info("Target Driver set to: {}", new Object[]{this.targetDriver});
        }
        return this;
    }

    /**
     * Set target table name to populate
     *
     * @param targetTableName table name
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
        if (logger != null) {
            logger.info("Target Table Name set to: {}", new Object[]{this.targetTableName});
        }
        return this;
    }

    /**
     * Set source HDFS directory to get the data from for export
     *
     * @param sourceHdfsDirectory HDFS directory
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setSourceHdfsDirectory(String sourceHdfsDirectory) {
        this.sourceHdfsDirectory = sourceHdfsDirectory;
        if (logger != null) {
            logger.info("Source HDFS Directory set to: {}", new Object[]{this.sourceHdfsDirectory});
        }
        return this;
    }

    /**
     * Set delimiter for source data in HDFS
     *
     * @param sourceHdfsFileDelimiter delimiter
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setSourceHdfsFileDelimiter(String sourceHdfsFileDelimiter) {
        this.sourceHdfsFileDelimiter = sourceHdfsFileDelimiter;
        if (logger != null) {
            logger.info("Source HDFS File Delimiter set to: {}", new Object[]{this.sourceHdfsFileDelimiter});
        }
        return this;
    }

    /**
     * Set strategy for identifying nulls in source data in HDFS
     *
     * @param sourceNullInterpretationStrategy null interpretation strategy
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setSourceNullInterpretationStrategy(ExportNullInterpretationStrategy sourceNullInterpretationStrategy) {
        this.sourceNullInterpretationStrategy = sourceNullInterpretationStrategy;
        if (logger != null) {
            logger.info("Source Null Interpretation Strategy set to: {}", new Object[]{this.sourceNullInterpretationStrategy});
        }
        return this;
    }

    /**
     * Set custom string for identifying null string values in HDFS data
     *
     * @param sourceNullInterpretationStrategyCustomNullString custom identifier string
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setSourceNullInterpretationStrategyCustomNullString(String sourceNullInterpretationStrategyCustomNullString) {
        this.sourceNullInterpretationStrategyCustomNullString = sourceNullInterpretationStrategyCustomNullString;
        if ((logger != null) && (this.sourceNullInterpretationStrategy == ExportNullInterpretationStrategy.CUSTOM_VALUES)) {
            logger.info("Strings having this value in HDFS data will be interpreted as null: {}", new Object[]{this.sourceNullInterpretationStrategyCustomNullString});
        }
        return this;
    }

    /**
     * Set custom string for identifying null non-string values in HDFS data
     *
     * @param sourceNullInterpretationStrategyCustomNullNonString custom identifier string
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setSourceNullInterpretationStrategyCustomNullNonString(String sourceNullInterpretationStrategyCustomNullNonString) {
        this.sourceNullInterpretationStrategyCustomNullNonString = sourceNullInterpretationStrategyCustomNullNonString;
        if ((logger != null) && (this.sourceNullInterpretationStrategy == ExportNullInterpretationStrategy.CUSTOM_VALUES)) {
            logger.info("Non-strings having this value in HDFS data will be interpreted as null: {}", new Object[]{this.sourceNullInterpretationStrategyCustomNullNonString});
        }
        return this;
    }

    /**
     * Set number of mappers to use for export
     *
     * @param clusterMapTasks number of mappers
     * @return {@link SqoopExportBuilder}
     */
    public SqoopExportBuilder setClusterMapTasks(Integer clusterMapTasks) {
        if (clusterMapTasks > 0) {
            this.clusterMapTasks = clusterMapTasks;
            if (logger != null) {
                logger.info("Number of Cluster Map Tasks set to: {}", new Object[]{this.clusterMapTasks});
            }
        }
        return this;
    }

    /**
     * Build a sqoop export command
     *
     * @return sqoop export command
     */
    public String build() {
        return buildSqoopExportCommand();
    }

    /*
     * Build the sqoop export command
     */
    private String buildSqoopExportCommand() {
        StringBuffer commandStringBuffer = new StringBuffer();

        /* Identify operation */
        commandStringBuffer.append(operationName)                                               //sqoop
            .append(SPACE_STRING)
            .append(operationType)                                                              //export
            .append(SPACE_STRING);

        /* Handle encrypted password file */
        if (passwordMode == PasswordMode.ENCRYPTED_ON_HDFS_FILE) {
            commandStringBuffer.append(targetPasswordLoaderClassLabel)                          //-Dorg.apache.sqoop.credentials.loader.class
                .append(EQUAL_STRING)
                .append(QUOTE)
                .append(targetPasswordLoaderClassValue)                                         //org.apache.sqoop.util.password.CryptoFileLoader
                .append(END_QUOTE_SPACE)
                .append(targetPasswordPassphraseLabel)                                          //-Dorg.apache.sqoop.credentials.loader.crypto.passphrase
                .append(EQUAL_STRING)
                .append(QUOTE)
                .append(targetPasswordPassphrase)                                               //"user provided"
                .append(END_QUOTE_SPACE);
        }

        /* Handle authentication */
        commandStringBuffer
            .append(targetConnectionStringLabel)                                                //--connect
            .append(START_SPACE_QUOTE)
            .append(targetConnectionString)                                                     //"user provided"
            .append(END_QUOTE_SPACE)
            .append(targetUserNameLabel)                                                        //--username
            .append(START_SPACE_QUOTE)
            .append(targetUserName)                                                             //"user provided"
            .append(END_QUOTE_SPACE);


        /* Handle password modes */
        if (passwordMode == PasswordMode.ENCRYPTED_ON_HDFS_FILE) {
            commandStringBuffer.append(targetPasswordHdfsFileLabel)                             //--password-file
                .append(START_SPACE_QUOTE)
                .append(targetPasswordHdfsFile)                                                 //"user provided"
                .append(END_QUOTE_SPACE);
        } else if (passwordMode == PasswordMode.CLEAR_TEXT_ENTRY || passwordMode == PasswordMode.ENCRYPTED_TEXT_ENTRY) {

            if (passwordMode == PasswordMode.ENCRYPTED_TEXT_ENTRY) {
                try {
                    targetEnteredPassword = DecryptPassword.decryptPassword(targetEnteredPassword, targetPasswordPassphrase);
                    logger.info("Entered encrypted password was decrypted successfully.");
                } catch (Exception e) {
                    targetEnteredPassword = UNABLE_TO_DECRYPT_STRING;
                    logger.warn("Unable to decrypt entered password (encrypted, Base 64). [{}]", new Object[]{e.getMessage()});
                }
            }

            commandStringBuffer.append(targetPasswordClearTextLabel)                            //--password
                .append(START_SPACE_QUOTE)
                .append(targetEnteredPassword)                                                  //"user provided"
                .append(END_QUOTE_SPACE);
        }

        if ((targetConnectionManager != null) && (!targetConnectionManager.isEmpty())) {
            commandStringBuffer.append(targetConnectionManagerLabel)                            //--connection-manager
                .append(START_SPACE_QUOTE)
                .append(targetConnectionManager)                                                //"user provided"
                .append(END_QUOTE_SPACE);
        }

        if ((targetDriver != null) && (!targetDriver.isEmpty())) {
            commandStringBuffer.append(targetDriverLabel)                                       //--driver
                .append(START_SPACE_QUOTE)
                .append(targetDriver)                                                           //"user provided"
                .append(END_QUOTE_SPACE);
        }

        /* Handle target table details */
        commandStringBuffer.append(targetTableNameLabel)                                        //--table
            .append(START_SPACE_QUOTE)
            .append(targetTableName)                                                            //"user provided"
            .append(END_QUOTE_SPACE);

        /* Handle HDFS source data parameters */
        commandStringBuffer.append(sourceHdfsDirectoryLabel)                                    //--export-dir
            .append(START_SPACE_QUOTE)
            .append(sourceHdfsDirectory)                                                        //"user provided"
            .append(END_QUOTE_SPACE)
            .append(sourceHdfsFileDelimiterLabel)                                               //--input-fields-terminated-by
            .append(START_SPACE_QUOTE)
            .append(sourceHdfsFileDelimiter)                                                    //"user provided"
            .append(END_QUOTE_SPACE);

        /* Handle HDFS source null interpretation strategy */
        if (sourceNullInterpretationStrategy != ExportNullInterpretationStrategy.SQOOP_DEFAULT) {
            if (sourceNullInterpretationStrategy == ExportNullInterpretationStrategy.HIVE_DEFAULT) {
                commandStringBuffer
                    .append(sourceNullInterpretationStrategyHiveDefaultNullStringLabelAndValue)     //--input-null-string '\\\\N'
                    .append(SPACE_STRING)
                    .append(sourceNullInterpretationStrategyHiveDefaultNullNonStringLabelAndValue)  //--input-null-non-string '\\\\N'
                    .append(SPACE_STRING);
            } else if (sourceNullInterpretationStrategy == ExportNullInterpretationStrategy.CUSTOM_VALUES) {
                commandStringBuffer
                    .append(sourceNullInterpretationStrategyCustomNullStringLabel)              //--input-null-string
                    .append(SPACE_STRING)
                    .append("'")
                    .append(sourceNullInterpretationStrategyCustomNullString)                   //"user provided"
                    .append("'")
                    .append(SPACE_STRING)
                    .append(sourceNullInterpretationStrategyCustomNullNonStringLabel)           //--input-null-non-string
                    .append(SPACE_STRING)
                    .append("'")
                    .append(sourceNullInterpretationStrategyCustomNullNonString)                //"user provided"
                    .append("'")
                    .append(SPACE_STRING);
            }
        }

        /* Handle other job parameters */
        commandStringBuffer.append(clusterMapTasksLabel)                                        //--num-mappers
            .append(START_SPACE_QUOTE)
            .append(clusterMapTasks)                                                            //"user provided"
            .append(END_QUOTE_SPACE);

        return commandStringBuffer.toString();
    }
}
