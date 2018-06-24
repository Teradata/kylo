package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils;

/*-
 * #%L
 * nifi-teradata-tdch-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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


import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchOperationType;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.logging.ComponentLog;

/**
 * A class to build a TDCH command that can be run on the command line
 */
public class TdchBuilder {

    private static final String EMPTY_STRING = "";
    private static final String SPACE_STRING = " ";
    private static final String START_SPACE_DOUBLE_QUOTE = " \"";
    private static final String END_DOUBLE_QUOTE_SPACE = "\" ";
    private static final String START_SPACE_SINGLE_QUOTE = " '";
    private static final String END_SINGLE_QUOTE_SPACE = "' ";
    private static final String QUOTE = "\"";
    private static final String EQUAL_STRING = "=";
    private static final String STAR_STRING = "*";
    private static final String MASK_STRING = "*****";
    private static final String DOLLAR = "$";

    private static final String tdchExportOperationToolClass = "com.teradata.connector.common.tool.ConnectorExportTool";
    private static final String tdchImportOperationToolClass = "com.teradata.connector.common.tool.ConnectorImportTool";

    private static final String hadoopCommandLabel = "hadoop jar";
    private static final String tdchLibraryJarsVariableLabel = "-libjars";

    private static final String commonTeradataClassnameLabel = "-classname";
    private static final String commonTeradataUrlLabel = "-url";
    private static final String commonTeradataUsernameLabel = "-username";
    private static final String commonTeradataPasswordLabel = "-password";

    private static final String commonExportToolMethodLabel = "-method";
    private static final String commonExportToolJobTypeLabel = "-jobtype";
    private static final String commonExportToolFileFormatLabel = "-fileformat";
    private static final String commonNumMappersLabel = "-nummappers";
    private static final String commonThrottleMappersLabel = "-throttlemappers";
    private static final String commonMinMappersLabel = "-minmappers";
    private static final String commonSourceDateFormatLabel = "-sourcedateformat";
    private static final String commonSourceTimeFormatLabel = "-sourcetimeformat";
    private static final String commonSourceTimestampFormatLabel = "-sourcetimestampformat";
    private static final String commonSourceTimezoneIdLabel = "-sourcetimezoneid";
    private static final String commonTargetDateFormatLabel = "-targetdateformat";
    private static final String commonTargetTimeFormatLabel = "-targettimeformat";
    private static final String commonTargetTimestampFormatLabel = "-targettimestampformat";
    private static final String commonTargetTimezoneIdLabel = "-targettimezoneid";
    private static final String commonStringTruncateLabel = "-stringtruncate";

    private static final String sourceHiveConfigurationFileHdfsPathLabel = "-hiveconf";
    private static final String sourceHiveSourceDatabaseLabel = "-sourcedatabase";
    private static final String sourceHiveSourceTableLabel = "-sourcetable";
    private static final String sourceHiveSourceFieldNamesLabel = "-sourcefieldnames";
    private static final String sourceHiveFieldSeparatorLabel = "-separator";
    private static final String sourceHiveLineSeparatorLabel = "-lineseparator";

    private static final String targetTeradataDatabaseTableLabel = "-targettable";
    private static final String targetTeradataFieldNamesLabel = "-targetfieldnames";
    private static final String targetTeradataTruncateLabel = "-Dtdch.output.teradata.truncate";
    private static final String targetTeradataUsexviewsLabel = "-usexviews";
    private static final String targetTeradataQueryBandLabel = "-queryband";
    private static final String targetTeradataBatchSizeLabel = "-batchsize";
    private static final String targetTeradataStagingDatabaseLabel = "-stagedatabase";
    private static final String targetTeradataStagingTableNameLabel = "-stagetablename";
    private static final String targetTeradataForceStageLabel = "-forcestage";
    private static final String targetTeradataKeepStageTableLabel = "-keepstagetable";
    private static final String targetTeradataFastLoadErrorTableDatabaseLabel = "-errortabledatabase";
    private static final String targetTeradataFastLoadErrorTableNameLabel = "-errortablename";


    private ComponentLog logger = null;
    private TdchOperationType tdchOperationType;

    private String commonTeradataClassname;
    private String commonTeradataUrl;
    private String commonTeradataUsername;
    private String commonTeradataPassword;

    private String commonExportToolMethod;
    private String commonExportToolJobType;
    private String commonExportToolFileFormat;
    private Integer commonNumMappers;
    private Boolean commonThrottleMappers;
    private Integer commonMinMappers;
    private String commonSourceDateFormat;
    private String commonSourceTimeFormat;
    private String commonSourceTimestampFormat;
    private String commonSourceTimezoneId;
    private String commonTargetDateFormat;
    private String commonTargetTimeFormat;
    private String commonTargetTimestampFormat;
    private String commonTargetTimezoneId;
    private Boolean commonStringTruncate;

    private String sourceHiveConfigurationFileHdfsPath;
    private String sourceHiveSourceDatabase;
    private String sourceHiveSourceTable;
    private String sourceHiveSourceFieldNames;
    private String sourceHiveFieldSeparator;
    private String sourceHiveLineSeparator;

    private String targetTeradataDatabaseTable;
    private String targetTeradataTargetFieldNames;
    private Boolean targetTeradataTruncate;
    private Boolean targetTeradataUseXviews;
    private String targetTeradataQueryBand;
    private Integer targetTeradataBatchSize;
    private String targetTeradataStagingDatabase;
    private String targetTeradataStagingTableName;
    private Boolean targetTeradataForceStage;
    private Boolean targetTeradataKeepStageTable;
    private String targetTeradataFastLoadErrorTableDatabase;
    private String targetTeradataFastLoadErrorTableName;

    private String tdchJarEnvironmentVariable;
    private String tdchLibraryJarsVariable;
    private String tdchHadoopClassPathVariable;

    public TdchBuilder setLogger(ComponentLog logger) {
        if (logger != null) {
            this.logger = logger;
            logger.info("Logger: {}", new Object[]{this.logger});
        }
        return this;
    }

    public TdchBuilder setTdchJarEnvironmentVariable(String tdchJarEnvironmentVariable) {
        if (StringUtils.isNotEmpty(tdchJarEnvironmentVariable)) {
            this.tdchJarEnvironmentVariable = DOLLAR + tdchJarEnvironmentVariable;
            logger.info("TDCH jar environment var: {}", new Object[]{this.tdchJarEnvironmentVariable});
        }
        return this;
    }

    public TdchBuilder setTdchLibraryJarsVariable(String tdchLibraryJarsVariable) {
        if (StringUtils.isNotEmpty(tdchLibraryJarsVariable)) {
            this.tdchLibraryJarsVariable = DOLLAR + tdchLibraryJarsVariable;
            logger.info("TDCH lib jars environment var: {}", new Object[]{this.tdchLibraryJarsVariable});
        }
        return this;
    }

    public TdchBuilder setTdchHadoopClassPathVariable(String tdchHadoopClassPathVariable) {
        if (StringUtils.isNotEmpty(tdchHadoopClassPathVariable)) {
            this.tdchHadoopClassPathVariable = DOLLAR + tdchHadoopClassPathVariable;
            logger.info("TDCH hadoop classpath environment var: {}", new Object[]{this.tdchHadoopClassPathVariable});
        }
        return this;
    }

    public TdchBuilder setTdchOperationType(TdchOperationType tdchOperationType) {
        if (tdchOperationType != null) {
            this.tdchOperationType = tdchOperationType;
            logger.info("Operation type: {}", new Object[]{this.tdchOperationType});
        }
        return this;
    }

    public TdchBuilder setCommonTeradataClassname(String targetTeradataClassname) {
        if (StringUtils.isNotEmpty(targetTeradataClassname)) {
            this.commonTeradataClassname = targetTeradataClassname;
            logger.info("Teradata JDBC driver class: {}", new Object[]{this.commonTeradataClassname});
        }
        return this;
    }

    public TdchBuilder setCommonTeradataUrl(String targetTeradataUrl, String teradataDatabaseTable) {
        if ((StringUtils.isNotEmpty(targetTeradataUrl)) &&
            (StringUtils.isNotBlank(teradataDatabaseTable)) && (StringUtils.contains(teradataDatabaseTable, "."))) {
            this.commonTeradataUrl = targetTeradataUrl + "/database=" + StringUtils.substring(teradataDatabaseTable, 0,
                                                                                              StringUtils.indexOf(teradataDatabaseTable, "."));
            logger.info("Teradata connection url: {}", new Object[]{this.commonTeradataUrl});
        }
        return this;
    }

    public TdchBuilder setCommonTeradataUsername(String targetTeradataUsername) {
        if (StringUtils.isNotEmpty(targetTeradataUsername)) {
            this.commonTeradataUsername = targetTeradataUsername;
            logger.info("Teradata user name: {}", new Object[]{this.commonTeradataUsername});
        }
        return this;
    }

    public TdchBuilder setCommonTeradataPassword(String targetTeradataPassword) {
        if (StringUtils.isNotEmpty(targetTeradataPassword)) {
            this.commonTeradataPassword = targetTeradataPassword;
            logger.info("Teradata password: {}", new Object[]{MASK_STRING});
        }
        return this;
    }

    public TdchBuilder setCommonExportToolMethod(String commonExportToolMethod) {
        if (commonExportToolMethod != null) {
            this.commonExportToolMethod = commonExportToolMethod;
            logger.info("Export method: {}", new Object[]{this.commonExportToolMethod});
        }
        return this;
    }

    public TdchBuilder setCommonExportToolJobType(String commonExportToolJobType) {
        if (commonExportToolJobType != null) {
            this.commonExportToolJobType = commonExportToolJobType;
            logger.info("Export job type: {}", new Object[]{this.commonExportToolJobType});
        }
        return this;
    }

    public TdchBuilder setCommonExportToolFileFormat(String commonExportToolFileFormat) {
        if (commonExportToolFileFormat != null) {
            this.commonExportToolFileFormat = commonExportToolFileFormat;
            logger.info("Export file format: {}", new Object[]{this.commonExportToolFileFormat});
        }
        return this;
    }

    public TdchBuilder setCommonNumMappers(Integer commonNumMappers) {
        if ((commonNumMappers != null) && (commonNumMappers > 0)) {
            this.commonNumMappers = commonNumMappers;
            logger.info("Number of mappers: {}", new Object[]{this.commonNumMappers});
        }
        return this;
    }

    public TdchBuilder setCommonThrottleMappers(Boolean commonThrottleMappers) {
        if (commonThrottleMappers != null) {
            this.commonThrottleMappers = commonThrottleMappers;
            logger.info("Throttle mappers? {}", new Object[]{this.commonThrottleMappers});
        }
        return this;
    }

    public TdchBuilder setCommonMinMappers(Integer commonMinMappers) {
        if ((commonMinMappers != null) && (commonMinMappers > 0)) {
            this.commonMinMappers = commonMinMappers;
            logger.info("Minimum mappers: {}", new Object[]{this.commonMinMappers});
        }
        return this;
    }

    public TdchBuilder setCommonSourceDateFormat(String commonSourceDateFormat) {
        if (StringUtils.isNotEmpty(commonSourceDateFormat)) {
            this.commonSourceDateFormat = commonSourceDateFormat;
            logger.info("Source date format: {}", new Object[]{this.commonSourceDateFormat});
        }
        return this;
    }

    public TdchBuilder setCommonSourceTimeFormat(String commonSourceTimeFormat) {
        if (StringUtils.isNotEmpty(commonSourceTimeFormat)) {
            this.commonSourceTimeFormat = commonSourceTimeFormat;
            logger.info("Source time format: {}", new Object[]{this.commonSourceTimeFormat});
        }
        return this;
    }

    public TdchBuilder setCommonSourceTimestampFormat(String commonSourceTimestampFormat) {
        if (StringUtils.isNotEmpty(commonSourceTimestampFormat)) {
            this.commonSourceTimestampFormat = commonSourceTimestampFormat;
            logger.info("Source timestamp format: {}", new Object[]{this.commonSourceTimestampFormat});
        }
        return this;
    }

    public TdchBuilder setCommonSourceTimezoneId(String commonSourceTimezoneId) {
        if (StringUtils.isNotEmpty(commonSourceTimezoneId)) {
            this.commonSourceTimezoneId = commonSourceTimezoneId;
            logger.info("Source timezone id: {}", new Object[]{this.commonSourceTimezoneId});
        }
        return this;
    }

    public TdchBuilder setCommonTargetDateFormat(String commonTargetDateFormat) {
        if (StringUtils.isNotEmpty(commonTargetDateFormat)) {
            this.commonTargetDateFormat = commonTargetDateFormat;
            logger.info("Target date format: {}", new Object[]{this.commonTargetDateFormat});
        }
        return this;
    }

    public TdchBuilder setCommonTargetTimeFormat(String commonTargetTimeFormat) {
        if (StringUtils.isNotEmpty(commonTargetTimeFormat)) {
            this.commonTargetTimeFormat = commonTargetTimeFormat;
            logger.info("Target time format: {}", new Object[]{this.commonTargetTimeFormat});
        }
        return this;
    }

    public TdchBuilder setCommonTargetTimestampFormat(String commonTargetTimestampFormat) {
        if (StringUtils.isNotEmpty(commonTargetTimestampFormat)) {
            this.commonTargetTimestampFormat = commonTargetTimestampFormat;
            logger.info("Target timestamp format: {}", new Object[]{this.commonTargetTimestampFormat});
        }
        return this;
    }

    public TdchBuilder setCommonTargetTimezoneId(String commonTargetTimezoneId) {
        if (StringUtils.isNotEmpty(commonTargetTimezoneId)) {
            this.commonTargetTimezoneId = commonTargetTimezoneId;
            logger.info("Target timezone id: {}", new Object[]{this.commonTargetTimezoneId});
        }
        return this;
    }

    public TdchBuilder setCommonStringTruncate(Boolean commonStringTruncate) {
        if (commonStringTruncate != null) {
            this.commonStringTruncate = commonStringTruncate;
            logger.info("String truncate? {}", new Object[]{this.commonStringTruncate});
        }
        return this;
    }

    public TdchBuilder setSourceHiveConfigurationFileHdfsPath(String sourceHiveConfigurationFileHdfsPath) {
        if (StringUtils.isNotEmpty(sourceHiveConfigurationFileHdfsPath)) {
            this.sourceHiveConfigurationFileHdfsPath = sourceHiveConfigurationFileHdfsPath;
            logger.info("Hive configuration file (HDFS path): {}", new Object[]{this.sourceHiveConfigurationFileHdfsPath});
        }
        return this;
    }

    public TdchBuilder setSourceHiveSourceDatabase(String sourceHiveSourceDatabase) {
        if (StringUtils.isNotEmpty(sourceHiveSourceDatabase)) {
            this.sourceHiveSourceDatabase = sourceHiveSourceDatabase;
            logger.info("Hive source database: {}", new Object[]{this.sourceHiveSourceDatabase});
        }
        return this;
    }

    public TdchBuilder setSourceHiveSourceTable(String sourceHiveSourceTable) {
        if (StringUtils.isNotEmpty(sourceHiveSourceTable)) {
            this.sourceHiveSourceTable = sourceHiveSourceTable;
            logger.info("Hive source table: {}", new Object[]{this.sourceHiveSourceTable});
        }
        return this;
    }

    public TdchBuilder setSourceHiveSourceFieldNames(String sourceHiveSourceFieldNames) {
        if (StringUtils.isNotEmpty(sourceHiveSourceFieldNames)) {
            this.sourceHiveSourceFieldNames = StringUtils.remove(sourceHiveSourceFieldNames, SPACE_STRING);
            logger.info("Hive source fields names: {}", new Object[]{this.sourceHiveSourceFieldNames});
        }
        return this;
    }

    public TdchBuilder setSourceHiveFieldSeparator(String sourceHiveFieldSeparator) {
        if (StringUtils.isNotEmpty(sourceHiveFieldSeparator)) {
            this.sourceHiveFieldSeparator = StringEscapeUtils.escapeJava(sourceHiveFieldSeparator);
            logger.info("Hive field separator: {}", new Object[]{this.sourceHiveFieldSeparator});
        }
        return this;
    }

    public TdchBuilder setSourceHiveLineSeparator(String sourceHiveLineSeparator) {
        if (StringUtils.isNotEmpty(sourceHiveLineSeparator)) {
            this.sourceHiveLineSeparator = StringEscapeUtils.escapeJava(sourceHiveLineSeparator);
            logger.info("Hive line separator: {}", new Object[]{this.sourceHiveLineSeparator});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataDatabaseTable(String targetTeradataDatabaseTable) {
        if (StringUtils.isNotEmpty(targetTeradataDatabaseTable)) {
            this.targetTeradataDatabaseTable = targetTeradataDatabaseTable;
            logger.info("Teradata database.table: {}", new Object[]{this.targetTeradataDatabaseTable});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataTargetFieldNames(String targetTeradataTargetFieldNames) {
        if (StringUtils.isNotEmpty(targetTeradataTargetFieldNames)) {
            this.targetTeradataTargetFieldNames = StringUtils.remove(targetTeradataTargetFieldNames, SPACE_STRING);
            logger.info("Teradata field names: {}", new Object[]{this.targetTeradataTargetFieldNames});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataTruncateTable(Boolean teradataTruncate) {
        if (teradataTruncate != null) {
            this.targetTeradataTruncate = teradataTruncate;
            logger.info("Teradata truncate? {}", new Object[]{this.targetTeradataTruncate});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataUseXviews(Boolean targetTeradataUseXviews) {
        if (targetTeradataUseXviews != null) {
            this.targetTeradataUseXviews = targetTeradataUseXviews;
            logger.info("Teradata use Xviews? {}", new Object[]{this.targetTeradataUseXviews});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataQueryBand(String targetTeradataQueryBand) {
        if (StringUtils.isNotEmpty(targetTeradataQueryBand)) {
            this.targetTeradataQueryBand = targetTeradataQueryBand;
            logger.info("Teradata query band: {}", new Object[]{this.targetTeradataQueryBand});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataBatchSize(Integer targetTeradataBatchSize) {
        if ((targetTeradataBatchSize != null) && (targetTeradataBatchSize > 0)) {
            this.targetTeradataBatchSize = targetTeradataBatchSize;
            logger.info("Teradata batch size: {}", new Object[]{this.targetTeradataBatchSize});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataStagingDatabase(String targetTeradataStagingDatabase) {
        if (StringUtils.isNotEmpty(targetTeradataStagingDatabase)) {
            this.targetTeradataStagingDatabase = targetTeradataStagingDatabase;
            logger.info("Teradata staging database: {}", new Object[]{this.targetTeradataStagingDatabase});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataStagingTableName(String targetTeradataStagingTableName) {
        if (StringUtils.isNotEmpty(targetTeradataStagingTableName)) {
            this.targetTeradataStagingTableName = targetTeradataStagingTableName;
            logger.info("Teradata staging table: {}", new Object[]{this.targetTeradataStagingTableName});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataForceStage(Boolean targetTeradataForceStage) {
        if (targetTeradataForceStage != null) {
            this.targetTeradataForceStage = targetTeradataForceStage;
            logger.info("Teradata force stage? {}", new Object[]{this.targetTeradataForceStage});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataKeepStageTable(Boolean targetTeradataKeepStageTable) {
        if (targetTeradataKeepStageTable != null) {
            this.targetTeradataKeepStageTable = targetTeradataKeepStageTable;
            logger.info("Teradata keep stage table? {}", new Object[]{this.targetTeradataKeepStageTable});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataFastLoadErrorTableDatabase(String targetTeradataFastLoadErrorTableDatabase) {
        if (StringUtils.isNotEmpty(targetTeradataFastLoadErrorTableDatabase)) {
            this.targetTeradataFastLoadErrorTableDatabase = targetTeradataFastLoadErrorTableDatabase;
            logger.info("Teradata fastload database: {}", new Object[]{this.targetTeradataFastLoadErrorTableDatabase});
        }
        return this;
    }

    public TdchBuilder setTargetTeradataFastLoadErrorTableName(String targetTeradataFastLoadErrorTableName) {
        if (StringUtils.isNotEmpty(targetTeradataFastLoadErrorTableName)) {
            this.targetTeradataFastLoadErrorTableName = targetTeradataFastLoadErrorTableName;
            logger.info("Teradata fastload error table: {}", new Object[]{this.targetTeradataFastLoadErrorTableName});
        }
        return this;
    }

    public String build() {
        if (tdchOperationType == null) {
            return EMPTY_STRING;
        }

        if (tdchOperationType.equals(TdchOperationType.TDCH_EXPORT) && commonExportToolJobType.equals("hive")) {
            //Export: Hive -> Teradata
            return buildTdchExportHiveToTeradataCommand();
        } else if (tdchOperationType.equals(TdchOperationType.TDCH_IMPORT)) {
            logger.warn("TDCH Import not yet implemented");
            return EMPTY_STRING;
        }
        logger.error("Unsupported TDCH operation (Operation Type: {}, Job Type: {})", new Object[]{tdchOperationType, commonExportToolJobType});
        return EMPTY_STRING;
    }

    private String buildTdchExportHiveToTeradataCommand() {
        StringBuffer commandStringBuffer = new StringBuffer();

        commandStringBuffer = buildValueNoQuotes(commandStringBuffer, hadoopCommandLabel, tdchJarEnvironmentVariable);

        commandStringBuffer.append(tdchExportOperationToolClass);
        commandStringBuffer.append(SPACE_STRING);

        commandStringBuffer = buildValueNoQuotes(commandStringBuffer, tdchLibraryJarsVariableLabel, tdchLibraryJarsVariable);

        if (targetTeradataTruncate != null) {
            commandStringBuffer = buildValueAsJavaArg(commandStringBuffer, targetTeradataTruncateLabel, targetTeradataTruncate);
        }

        if (StringUtils.isNotEmpty(commonTeradataClassname)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTeradataClassnameLabel, commonTeradataClassname);
        }

        if (StringUtils.isNotEmpty(commonTeradataUrl)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTeradataUrlLabel, commonTeradataUrl);
        }

        if (StringUtils.isNotEmpty(commonTeradataUsername)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTeradataUsernameLabel, commonTeradataUsername);
        }

        if (StringUtils.isNotEmpty(commonTeradataPassword)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTeradataPasswordLabel, commonTeradataPassword);
        }

        if (StringUtils.isNotEmpty(commonExportToolMethod)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonExportToolMethodLabel, commonExportToolMethod);
        }

        if (StringUtils.isNotEmpty(commonExportToolJobType)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonExportToolJobTypeLabel, commonExportToolJobType);
        }

        if (StringUtils.isNotEmpty(commonExportToolFileFormat)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonExportToolFileFormatLabel, commonExportToolFileFormat);
        }

        if ((commonNumMappers != null) && (commonNumMappers > 0)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonNumMappersLabel, commonNumMappers);
        }

        if (commonThrottleMappers != null) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonThrottleMappersLabel, commonThrottleMappers);
            if (commonThrottleMappers) {
                if ((commonMinMappers != null) && (commonMinMappers > 0)) {
                    commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonMinMappersLabel, commonMinMappers);
                }
            }
        }

        if (StringUtils.isNotEmpty(commonSourceDateFormat)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonSourceDateFormatLabel, commonSourceDateFormat);
        }

        if (StringUtils.isNotEmpty(commonSourceTimeFormat)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonSourceTimeFormatLabel, commonSourceTimeFormat);
        }

        if (StringUtils.isNotEmpty(commonSourceTimestampFormat)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonSourceTimestampFormatLabel, commonSourceTimestampFormat);
        }

        if (StringUtils.isNotEmpty(commonSourceTimezoneId)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonSourceTimezoneIdLabel, commonSourceTimezoneId);
        }

        if (StringUtils.isNotEmpty(commonTargetDateFormat)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTargetDateFormatLabel, commonTargetDateFormat);
        }

        if (StringUtils.isNotEmpty(commonTargetTimeFormat)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTargetTimeFormatLabel, commonTargetTimeFormat);
        }

        if (StringUtils.isNotEmpty(commonTargetTimestampFormat)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTargetTimestampFormatLabel, commonTargetTimestampFormat);
        }

        if (StringUtils.isNotEmpty(commonTargetTimezoneId)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonTargetTimezoneIdLabel, commonTargetTimezoneId);
        }

        if (commonStringTruncate != null) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, commonStringTruncateLabel, commonStringTruncate);
        }

        if (StringUtils.isNotEmpty(sourceHiveConfigurationFileHdfsPath)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, sourceHiveConfigurationFileHdfsPathLabel, sourceHiveConfigurationFileHdfsPath);
        }

        if (StringUtils.isNotEmpty(sourceHiveSourceDatabase)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, sourceHiveSourceDatabaseLabel, sourceHiveSourceDatabase);
        }

        if (StringUtils.isNotEmpty(sourceHiveSourceTable)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, sourceHiveSourceTableLabel, sourceHiveSourceTable);
        }

        if (StringUtils.isNotEmpty(sourceHiveSourceFieldNames)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, sourceHiveSourceFieldNamesLabel, sourceHiveSourceFieldNames);
        }

        if (StringUtils.isNotEmpty(sourceHiveFieldSeparator)) {
            commandStringBuffer = buildValueNoQuotes(commandStringBuffer, sourceHiveFieldSeparatorLabel, sourceHiveFieldSeparator);
        }

        //No quotes
        if (StringUtils.isNotEmpty(sourceHiveLineSeparator)) {
            commandStringBuffer = buildValueNoQuotes(commandStringBuffer, sourceHiveLineSeparatorLabel, sourceHiveLineSeparator);
        }

        if (StringUtils.isNotEmpty(targetTeradataDatabaseTable)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataDatabaseTableLabel, targetTeradataDatabaseTable);
        }

        if (StringUtils.isNotEmpty(targetTeradataTargetFieldNames)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataFieldNamesLabel, targetTeradataTargetFieldNames);
        }

        if (targetTeradataUseXviews != null) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataUsexviewsLabel, targetTeradataUseXviews);
        }

        if (StringUtils.isNotEmpty(targetTeradataQueryBand)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataQueryBandLabel, targetTeradataQueryBand);
        }

        if ((targetTeradataBatchSize != null) && (targetTeradataBatchSize > 0)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataBatchSizeLabel, targetTeradataBatchSize);
        }

        if (StringUtils.isNotEmpty(targetTeradataStagingDatabase)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataStagingDatabaseLabel, targetTeradataStagingDatabase);
        }

        if (StringUtils.isNotEmpty(targetTeradataStagingTableName)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataStagingTableNameLabel, targetTeradataStagingTableName);
        }

        if (targetTeradataForceStage != null) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataForceStageLabel, targetTeradataForceStage);
        }

        if (targetTeradataKeepStageTable != null) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataKeepStageTableLabel, targetTeradataKeepStageTable);
        }

        if (StringUtils.isNotEmpty(targetTeradataFastLoadErrorTableDatabase)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataFastLoadErrorTableDatabaseLabel, targetTeradataFastLoadErrorTableDatabase);
        }

        if (StringUtils.isNotEmpty(targetTeradataFastLoadErrorTableName)) {
            commandStringBuffer = buildValueWithDoubleQuotes(commandStringBuffer, targetTeradataFastLoadErrorTableNameLabel, targetTeradataFastLoadErrorTableName);
        }

        return commandStringBuffer.toString();
    }

    private StringBuffer buildValueAsJavaArg(StringBuffer sb, String label, Object value) {
        sb.append(label);
        sb.append(EQUAL_STRING);
        sb.append(value.toString());
        sb.append(SPACE_STRING);
        return sb;
    }

    private StringBuffer buildValueNoQuotes(StringBuffer sb, String label, Object value) {
        sb.append(label);
        sb.append(SPACE_STRING);
        sb.append(value.toString());
        sb.append(SPACE_STRING);
        return sb;
    }

    private StringBuffer buildValueWithSingleQuotes(StringBuffer sb, String label, Object value) {
        sb.append(label);
        sb.append(START_SPACE_SINGLE_QUOTE);
        sb.append(value.toString());
        sb.append(END_SINGLE_QUOTE_SPACE);
        return sb;
    }

    private StringBuffer buildValueWithDoubleQuotes(StringBuffer sb, String label, Object value) {
        sb.append(label);
        sb.append(START_SPACE_DOUBLE_QUOTE);
        sb.append(value.toString());
        sb.append(END_DOUBLE_QUOTE_SPACE);
        return sb;
    }

    public static String getCommonTeradataPasswordLabel() {
        return commonTeradataPasswordLabel;
    }
}

