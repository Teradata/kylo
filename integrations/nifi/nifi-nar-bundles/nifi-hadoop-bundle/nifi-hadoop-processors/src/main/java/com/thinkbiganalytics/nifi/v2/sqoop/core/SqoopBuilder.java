package com.thinkbiganalytics.nifi.v2.sqoop.core;

import com.thinkbiganalytics.nifi.v2.sqoop.enums.CompressionAlgorithm;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.HiveDelimStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.HiveNullEncodingStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.SqoopLoadStrategy;

import org.apache.nifi.logging.ComponentLog;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * @author jagrut sharma
 */

/*
A class to build a sqoop command that can be run on the command line
 */
public class SqoopBuilder {
    private final String SPACE_STRING = " ";
    private final String START_SPACE_QUOTE = " \"";
    private final String END_QUOTE_SPACE = "\" ";
    private final String QUOTE = "\"";
    private final String EQUAL_STRING = "=";
    private final String STAR_STRING = "*";
    private final Integer DEFAULT_CLUSTER_MAP_TASKS = 4;

    private final String sourcePasswordLoaderClassLabel = "-Dorg.apache.sqoop.credentials.loader.class";
    private final String sourcePasswordLoaderClassValue = "org.apache.sqoop.util.password.CryptoFileLoader";
    private final String sourcePasswordPassphraseLabel = "-Dorg.apache.sqoop.credentials.loader.crypto.passphrase";
    private String sourcePasswordPassphrase;
    private final String sourceDriverLabel = "--driver";
    private String sourceDriver;
    private final String sourceConnectionStringLabel = "--connect";
    private String sourceConnectionString;
    private final String sourceUserNameLabel = "--username";
    private String sourceUserName;
    private final String sourcePasswordHdfsFileLabel = "--password-file";
    private String sourcePasswordHdfsFile;
    private final String sourceTableNameLabel = "--table";
    private String sourceTableName;
    private final String sourceTableFieldsLabel = "--columns";
    private String sourceTableFields;
    private final String sourceTableWhereClauseLabel = "--where";
    private String sourceTableWhereClause;
    private final String sourceTableSplitFieldLabel = "--split-by";
    private String sourceTableSplitField;
    private final String targetHdfsDirectoryLabel = "--target-dir";
    private String targetHdfsDirectory;
    private final String sourceAutoSetToOneMapperLabel = "--autoreset-to-one-mapper";
    private final String clusterMapTasksLabel = "--num-mappers";
    private Integer clusterMapTasks = DEFAULT_CLUSTER_MAP_TASKS;
    private SqoopLoadStrategy sourceLoadStrategy;
    private final String incrementalStrategyLabel = "--incremental";
    private final String incrementalAppendStrategyLabel = "append";
    private final String incrementalLastModifiedStrategyLabel = "lastmodified";
    private final String sourceCheckColumnNameLabel = "--check-column";
    private String sourceCheckColumnName;
    private final String sourceCheckColumnLastValueLabel = "--last-value";
    private String sourceCheckColumnLastValue;
    private final String sourceBoundaryQueryLabel = "--boundary-query";
    private String sourceBoundaryQuery;
    private final String clusterUIJobNameLabel = "--mapreduce-job-name";
    private String clusterUIJobName;
    private HiveDelimStrategy targetHiveDelimStrategy;
    private final String targetHiveDropDelimLabel = "--hive-drop-import-delims";
    private final String targetHiveReplaceDelimLabel = "--hive-delims-replacement";
    private String targetHiveReplaceDelim;
    private final HiveNullEncodingStrategy targetHiveNullEncodingStrategy = HiveNullEncodingStrategy.ENCODE_STRING_AND_NONSTRING;
    private final String targetHiveNullEncodingStrategyNullStringLabel = "--null-string '\\\\N'";
    private final String targetHiveNullEncodingStrategyNullNonStringLabel = "--null-non-string '\\\\N'";
    private final String targetHdfsFileDelimiterLabel = "--fields-terminated-by";
    private String targetHdfsFileDelimiter;
    private final String targetCompressLabel = "--compress";
    private final String targetCompressionCodecLabel = "--compression-codec";
    private String targetCompressionCodec;
    private CompressionAlgorithm targetCompressionAlgorithm;
    private Boolean targetCompressFlag = false;

    private String feedCategory;
    private String feedName;

    private final String operationName = "sqoop";
    private final String operationType = "import";

    private ComponentLog logger = null;

    public SqoopBuilder setLogger (ComponentLog logger) {
        this.logger = logger;
        logger.info("Logger set to {}", new Object[]{this.logger});
        return this;
    }

    public SqoopBuilder setFeedCategory (String feedCategory) {
        this.feedCategory = feedCategory;
        logMessage("info", "Feed Category", this.feedCategory);
        return this;
    }

    public SqoopBuilder setFeedName (String feedName) {
        this.feedName = feedName;
        logMessage("info", "Feed Name", this.feedName);
        return this;
    }

    public SqoopBuilder setSourceDriver (String sourceDriver) {
        this.sourceDriver = sourceDriver;
        logMessage("info", "Source Driver", this.sourceDriver);
        return this;
    }

    public SqoopBuilder setSourceConnectionString (String sourceConnectionString) {
        this.sourceConnectionString = sourceConnectionString;
        logMessage("info", "Source Connection String", this.sourceConnectionString);
        return this;
    }

    public SqoopBuilder setSourceUserName (String sourceUserName) {
        this.sourceUserName = sourceUserName;
        logMessage("info", "Source User Name", this.sourceUserName);
        return this;
    }

    public SqoopBuilder setSourcePasswordHdfsFile (String sourcePasswordHdfsFile) {
        this.sourcePasswordHdfsFile = sourcePasswordHdfsFile;
        logMessage("info", "Source Password File (HDFS)", this.sourcePasswordHdfsFile);
        return this;
    }

    public SqoopBuilder setSourcePasswordPassphrase (String sourcePasswordPassphrase) {
        this.sourcePasswordPassphrase = sourcePasswordPassphrase;
        logMessage("info", "Source Password Passphrase", this.sourcePasswordPassphrase);
        return this;
    }

    public SqoopBuilder setSourceTableName (String sourceTableName) {
        this.sourceTableName = sourceTableName;
        logMessage("info", "Source Table Name", this.sourceTableName);
        return this;
    }

    public SqoopBuilder setSourceTableFields (String sourceTableFields) {
        if (sourceTableFields.trim().equals(STAR_STRING)) {
            // all fields
            this.sourceTableFields = sourceTableFields;
        }
        else {
            // selected fields
            List<String> fieldList = Arrays.asList(sourceTableFields.split(","));
            this.sourceTableFields = getQueryFieldsForStatement(fieldList);
        }
        logMessage("info", "Source Table Fields", this.sourceTableFields);
        return this;
    }

    public SqoopBuilder setSourceTableWhereClause (String sourceTableWhereClause) {
        this.sourceTableWhereClause = sourceTableWhereClause;
        logMessage("info", "Source Table Where Clause", sourceTableWhereClause);
        return this;
    }

    public SqoopBuilder setSourceLoadStrategy (SqoopLoadStrategy sourceLoadStrategy) {
        this.sourceLoadStrategy = sourceLoadStrategy;
        logMessage("info", "Source Load Strategy", this.sourceLoadStrategy);
        return this;
    }

    public SqoopBuilder setSourceCheckColumnName (String sourceCheckColumnName) {
        this.sourceCheckColumnName = sourceCheckColumnName;
        logMessage("info", "Source Check Column Name", this.sourceCheckColumnName);
        return this;
    }

    public SqoopBuilder setSourceCheckColumnLastValue (String sourceCheckColumnLastValue) {
        this.sourceCheckColumnLastValue = sourceCheckColumnLastValue;
        logMessage("info", "Source Check Column Last Value", this.sourceCheckColumnLastValue);
        return this;
    }

    public SqoopBuilder setSourceSplitByField (String sourceSplitByField) {
        this.sourceTableSplitField = sourceSplitByField;
        logMessage("info", "Source Split By Field", this.sourceTableSplitField);
        return this;
    }

    public SqoopBuilder setSourceBoundaryQuery (String sourceBoundaryQuery) {
        this.sourceBoundaryQuery = sourceBoundaryQuery;
        logMessage("info", "Source Boundary Query", this.sourceBoundaryQuery);
        return this;
    }

    public SqoopBuilder setClusterMapTasks (Integer clusterMapTasks) {
        if (clusterMapTasks > 0) {
            this.clusterMapTasks = clusterMapTasks;
            logMessage("info", "Number of Cluster Map Tasks", this.clusterMapTasks);
        }
        return this;
    }

    public SqoopBuilder setClusterUIJobName (String clusterUIJobName) {
        this.clusterUIJobName = clusterUIJobName;
        logMessage("info", "Cluster UI Job Name", this.clusterUIJobName);
        return this;
    }

    public SqoopBuilder setTargetHdfsDirectory (String targetHdfsDirectory) {
        this.targetHdfsDirectory = targetHdfsDirectory;
        logMessage("info", "Target HDFS Directory", this.targetHdfsDirectory);
        return this;
    }

    public SqoopBuilder setTargetHdfsFileDelimiter (String targetHdfsFileDelimiter) {
        this.targetHdfsFileDelimiter = targetHdfsFileDelimiter;
        logMessage("info", "Target HDFS File Delimiter", this.targetHdfsFileDelimiter);
        return this;
    }

    public SqoopBuilder setTargetHiveDelimStrategy (HiveDelimStrategy targetHiveDelimStrategy) {
        this.targetHiveDelimStrategy = targetHiveDelimStrategy;
        logMessage("info", "Target Hive Delimiter Strategy", this.targetHiveDelimStrategy);
        return this;
    }

    public SqoopBuilder setTargetHiveReplaceDelim (String targetHiveReplaceDelim) {
        this.targetHiveReplaceDelim = targetHiveReplaceDelim;
        logMessage("info", "Target Hive Replace Delim", this.targetHiveReplaceDelim);
        return this;
    }

    public SqoopBuilder setTargetCompressionAlgorithm (CompressionAlgorithm targetCompressionAlgorithm) {
        this.targetCompressionAlgorithm = targetCompressionAlgorithm;
        targetCompressionCodec = getCompressionCodecClass (this.targetCompressionAlgorithm);
        logMessage("info", "Target Compression Algorithm", this.targetCompressionAlgorithm);
        return this;
    }

    private String getCompressionCodecClass (CompressionAlgorithm compressionAlgorithm) {
        String compressionCodecClass = null;

        if (compressionAlgorithm != null) {
            switch (compressionAlgorithm) {
                case NONE:
                    compressionCodecClass = null;
                    break;

                case GZIP:
                    compressionCodecClass = "org.apache.hadoop.io.compress.GzipCodec";
                    break;

                case SNAPPY:
                    compressionCodecClass = "org.apache.hadoop.io.compress.SnappyCodec";
                    break;

                case BZIP2:
                    compressionCodecClass = "org.apache.hadoop.io.compress.BZip2Codec";
                    break;

                case LZO:
                    compressionCodecClass = "com.hadoop.compression.lzo.LzoCodec";
                    break;
            }
        }

        logMessage("info", "Compression Codec", this.targetCompressionCodec);

        if (compressionCodecClass != null) {
            targetCompressFlag = true;
            logMessage("info", "Compress output", targetCompressFlag);
        }

        return compressionCodecClass;
    }

    private void logMessage(@Nonnull String level, @Nonnull String property, @Nonnull Object value) {
        switch (level) {
            case "debug":
                if (logger != null) {
                    logger.debug("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "info":
                if (logger != null) {
                    logger.info("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "trace":
                if (logger != null) {
                    logger.trace("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "warn":
                if (logger != null) {
                    logger.warn("{} set to: {}", new Object[] { property, value});
                }
                break;

            case "error":
                if (logger != null) {
                    logger.error("{} set to: {}", new Object[] { property, value});
                }
                break;
        }
    }

    public String build() {
        return buildSqoopCommand();
    }

    private String buildSqoopCommand() {
        StringBuffer commandStringBuffer = new StringBuffer();

        commandStringBuffer.append(operationName)   //sqoop
            .append(SPACE_STRING)
            .append(operationType)                  //import
            .append(SPACE_STRING)
            .append(sourcePasswordLoaderClassLabel) //-Dorg.apache.sqoop.credentials.loader.class
            .append(EQUAL_STRING)
            .append(QUOTE)
            .append(sourcePasswordLoaderClassValue) //org.apache.sqoop.util.password.CryptoFileLoader
            .append(END_QUOTE_SPACE)
            .append(sourcePasswordPassphraseLabel)  //-Dorg.apache.sqoop.credentials.loader.crypto.passphrase
            .append(EQUAL_STRING)
            .append(QUOTE)
            .append(sourcePasswordPassphrase)       //"user provided"
            .append(END_QUOTE_SPACE)
            .append(sourceDriverLabel)              //--driver
            .append(START_SPACE_QUOTE)
            .append(sourceDriver)                   //"user provided"
            .append(END_QUOTE_SPACE)
            .append(sourceConnectionStringLabel)    //--connect
            .append(START_SPACE_QUOTE)
            .append(sourceConnectionString)         //"user provided"
            .append(END_QUOTE_SPACE)
            .append(sourceUserNameLabel)            //--username
            .append(START_SPACE_QUOTE)
            .append(sourceUserName)                 //"user provided"
            .append(END_QUOTE_SPACE)
            .append(sourcePasswordHdfsFileLabel)    //--password-file
            .append(START_SPACE_QUOTE)
            .append(sourcePasswordHdfsFile)         //"user provided"
            .append(END_QUOTE_SPACE)
            .append(sourceTableNameLabel)           //--table
            .append(START_SPACE_QUOTE)
            .append(sourceTableName)                //"user provided"
            .append(END_QUOTE_SPACE);

        if (!sourceTableFields.equals(STAR_STRING)) {
            commandStringBuffer
                .append(sourceTableFieldsLabel)         //--columns
                .append(START_SPACE_QUOTE)
                .append(sourceTableFields)              //"generated from user provided value"
                .append(END_QUOTE_SPACE);
        }

        if (sourceTableWhereClause != null) {
            commandStringBuffer
                .append(sourceTableWhereClauseLabel)    //--where
                .append(START_SPACE_QUOTE)
                .append(sourceTableWhereClause)         //"user provided"
                .append(END_QUOTE_SPACE);
        }

        if (sourceTableSplitField != null) {
            commandStringBuffer
                .append(sourceTableSplitFieldLabel)     //--split-by
                .append(START_SPACE_QUOTE)
                .append(sourceTableSplitField)          //"user provided"
                .append(END_QUOTE_SPACE);
        }
        else {
            commandStringBuffer
                .append(sourceAutoSetToOneMapperLabel)  //--autoreset-to-one-mapper
                .append(SPACE_STRING);
        }

        commandStringBuffer
            .append(targetHdfsDirectoryLabel)       //--target-dir
            .append(START_SPACE_QUOTE)
            .append(targetHdfsDirectory)            //"user provided"
            .append(END_QUOTE_SPACE)
            .append(targetHdfsFileDelimiterLabel)   //--fields-terminated-by
            .append(START_SPACE_QUOTE)
            .append(targetHdfsFileDelimiter)        //"user provided"
            .append(END_QUOTE_SPACE);

        if (sourceLoadStrategy != SqoopLoadStrategy.FULL_LOAD) {
            commandStringBuffer
                .append(incrementalStrategyLabel)   //--incremental
                .append(SPACE_STRING);

            if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND) {
                commandStringBuffer.append(incrementalAppendStrategyLabel); //append
            }
            else if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED) {
                commandStringBuffer.append(incrementalLastModifiedStrategyLabel);   //lastmodified
            }

            commandStringBuffer
                .append(SPACE_STRING)
                .append(sourceCheckColumnNameLabel) //--check-column
                .append(START_SPACE_QUOTE)
                .append(sourceCheckColumnName)
                .append(END_QUOTE_SPACE)
                .append(sourceCheckColumnLastValueLabel) //--last-value
                .append(START_SPACE_QUOTE)
                .append(sourceCheckColumnLastValue)
                .append(END_QUOTE_SPACE);
        }

        if (targetHiveDelimStrategy == HiveDelimStrategy.DROP) {
            commandStringBuffer
                .append(targetHiveDropDelimLabel)   //--hive-drop-import-delims
                .append(SPACE_STRING);
        }
        else if (targetHiveDelimStrategy == HiveDelimStrategy.KEEP) {
            // Do nothing. Keeping for readability.
        }
        else if (targetHiveDelimStrategy == HiveDelimStrategy.REPLACE) {
            commandStringBuffer
                .append(targetHiveReplaceDelimLabel)    //--hive-delims-replacement
                .append(START_SPACE_QUOTE)
                .append(targetHiveReplaceDelim)         //"user provided"
                .append(END_QUOTE_SPACE);
        }

        if (targetHiveNullEncodingStrategy == HiveNullEncodingStrategy.ENCODE_STRING_AND_NONSTRING) {
            commandStringBuffer
                .append(targetHiveNullEncodingStrategyNullStringLabel)  //--null-string '\\\\N'
                .append(SPACE_STRING)
                .append(targetHiveNullEncodingStrategyNullNonStringLabel)   //--null-non-string '\\\\N'
                .append(SPACE_STRING);
        }
        else if (targetHiveNullEncodingStrategy == HiveNullEncodingStrategy.DO_NOT_ENCODE) {
            // Do nothing. Keeping for readability.
        }
        else if (targetHiveNullEncodingStrategy == HiveNullEncodingStrategy.ENCODE_ONLY_STRING) {
            commandStringBuffer
                .append(targetHiveNullEncodingStrategyNullStringLabel)  //--null-string '\\\\N'
                .append(SPACE_STRING);
        }
        else if (targetHiveNullEncodingStrategy == HiveNullEncodingStrategy.ENCODE_ONLY_NONSTRING) {
            commandStringBuffer
                .append(targetHiveNullEncodingStrategyNullNonStringLabel)   //--null-non-string '\\\\N'
                .append(SPACE_STRING);
        }

        if (sourceBoundaryQuery != null) {
            commandStringBuffer
                .append(sourceBoundaryQueryLabel)   //--boundary-query
                .append(START_SPACE_QUOTE)
                .append(sourceBoundaryQuery)        //"user provided"
                .append(END_QUOTE_SPACE);
        }

        commandStringBuffer.append(clusterMapTasksLabel)    //--num-mappers
            .append(START_SPACE_QUOTE)
            .append(clusterMapTasks)                //"user-provided-value"
            .append(END_QUOTE_SPACE);

        if (targetCompressFlag) {
            commandStringBuffer
                .append(targetCompressLabel) //--compress
                .append(SPACE_STRING)
                .append(targetCompressionCodecLabel)    //--compression-codec
                .append(START_SPACE_QUOTE)
                .append(targetCompressionCodec)
                .append(END_QUOTE_SPACE);
        }

        if (clusterUIJobName != null) {
            commandStringBuffer
                .append(clusterUIJobNameLabel)      //--mapreduce-job-name
                .append(START_SPACE_QUOTE)
                .append(clusterUIJobName)           //"user-provided-value"
                .append(QUOTE);
        }

        return commandStringBuffer.toString();
    }

    /*
    Get the list of fields as a string separated by commas
     */
    private String getQueryFieldsForStatement(List<String> fields) {
        int totalFields = fields.size();
        StringBuilder queryFieldsBuilder = new StringBuilder();

        for (int i = 0; i < totalFields; i++) {
            queryFieldsBuilder.append(fields.get(i).trim());
            if (i != totalFields -1) {
                queryFieldsBuilder.append(",");
            }
        }
        return queryFieldsBuilder.toString();
    }
}
