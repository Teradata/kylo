package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base;

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


import com.thinkbiganalytics.kylo.nifi.teradata.tdch.api.TdchConnectionService;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A base class for NiFi processors that use TDCH
 */
public abstract class AbstractTdchProcessor extends AbstractNiFiProcessor {

    //Common properties
    protected static final String IMPORT_TOOL_METHOD_NAME = "Import tool method";
    protected static final String EXPORT_TOOL_METHOD_NAME = "Export tool method";

    protected static final String IMPORT_TOOL_JOB_TYPE_NAME = "Import tool job type";
    protected static final String IMPORT_TOOL_FILEFORMAT_NAME = "Import tool file format";

    protected static final String EXPORT_TOOL_JOB_TYPE_NAME = "Export tool job type";
    protected static final String EXPORT_TOOL_FILEFORMAT_NAME = "Export tool file format";

    protected static final String NUMBER_OF_MAPPERS_NAME = "[Cluster] Number of mappers";
    protected static final String THROTTLE_MAPPERS_FLAG_NAME = "[Cluster] Throttle mappers?";
    protected static final String MINIMUM_MAPPERS_NAME = "[Cluster] Minimum mappers";

    protected static final String SOURCE_DATE_FORMAT_NAME = "Source date format";
    protected static final String SOURCE_TIME_FORMAT_NAME = "Source time format";
    protected static final String SOURCE_TIMESTAMP_FORMAT_NAME = "Source timestamp format";
    protected static final String SOURCE_TIMEZONE_ID_NAME = "Source timezone";

    protected static final String TARGET_DATE_FORMAT_NAME = "Target date format";
    protected static final String TARGET_TIME_FORMAT_NAME = "Target time format";
    protected static final String TARGET_TIMESTAMP_FORMAT_NAME = "Target timestamp format";
    protected static final String TARGET_TIMEZONE_ID_NAME = "Target timezone";

    protected static final String STRING_TRUNCATE_FLAG_NAME = "Truncate strings?";

    //Connection service
    protected static final String TDCH_CONNECTION_SERVICE_NAME = "[Teradata] TDCH connection service";

    //Env vars
    protected static final String TDCH_JAR_PATH_ENV_VAR_NAME = "USERLIBTDCH";
    protected static final String TDCH_LIB_JARS_ENV_VAR_NAME = "LIB_JARS";
    protected static final String TDCH_HADOOP_CLASSPATH_ENV_VAR_NAME = "HADOOP_CLASSPATH";

    //Kerberos. Use in concrete classes extending this base class.
    protected PropertyDescriptor KERBEROS_PRINCIPAL;
    protected PropertyDescriptor KERBEROS_KEYTAB;

    //Shell
    protected static final String COMMAND_SHELL = "/bin/bash";
    protected static final String COMMAND_SHELL_FLAGS = "-c";

    //Defaults
    protected static final String DEFAULT_IMPORT_TOOL_METHOD = "split.by.hash";
    protected static final String DEFAULT_IMPORT_TOOL_JOB_TYPE = "hive";
    protected static final String DEFAULT_IMPORT_TOOL_FILEFORMAT = "textfile";

    protected static final String DEFAULT_EXPORT_TOOL_METHOD = "batch.insert";
    protected static final String DEFAULT_EXPORT_TOOL_JOB_TYPE = "hive";
    protected static final String DEFAULT_EXPORT_TOOL_FILEFORMAT = "textfile";

    protected static final String DEFAULT_NUMBER_OF_MAPPERS = "2";
    protected static final String DEFAULT_THROTTLE_MAPPERS_FLAG = "false";
    protected static final String DEFAULT_STRING_TRUNCATE_FLAG = "true";
    protected static final String DEFAULT_SOURCE_DATE_FORMAT = "yyyy-MM-dd";
    protected static final String DEFAULT_SOURCE_TIME_FORMAT = "HH:mm:ss";
    protected static final String DEFAULT_SOURCE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    protected static final String DEFAULT_TARGET_DATE_FORMAT = "yyyy-MM-dd";
    protected static final String DEFAULT_TARGET_TIME_FORMAT = "HH:mm:ss";
    protected static final String DEFAULT_TARGET_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    //Valid import tool methods
    static final String[] validImportToolMethods = new String[]{
        "split.by.hash",
        "split.by.value",
        "split.by.partition",
        "split.by.amp",
        "internal.fastload",
        "${tdch.import.tool.method}"
    };

    //Valid import tool job types
    static final String[] validImportToolJobTypes = new String[]{
        "hive",
        "${tdch.import.tool.job.type}"
    };

    //Valid import tool file formats
    static final String[] validImportToolFileFormats = new String[]{
        "textfile",
        "sequencefile",
        "rcfile",
        "orcfile",
        "parquet",
        "${tdch.import.tool.file.format}"
    };

    //Valid export tool methods
    static final String[] validExportToolMethods = new String[]{
        "batch.insert",
        "internal.fastload",
        "${tdch.export.tool.method}"
    };

    //Valid export tool job types
    static final String[] validExportToolJobTypes = new String[]{
        "hive",
        "${tdch.export.tool.job.type}"
    };

    //Valid export tool file formats
    static final String[] validExportToolFileFormats = new String[]{
        "textfile",
        "sequencefile",
        "rcfile",
        "orcfile",
        "parquet",
        "${tdch.export.tool.file.format}"
    };

    public static final PropertyDescriptor TDCH_CONNECTION_SERVICE = new PropertyDescriptor.Builder()
        .name(TDCH_CONNECTION_SERVICE_NAME)
        .description("Controller service for connecting to a Teradata system for running a TDCH job")
        .required(true)
        .identifiesControllerService(TdchConnectionService.class)
        .build();

    public static final PropertyDescriptor IMPORT_TOOL_METHOD = new PropertyDescriptor.Builder()
        .name(IMPORT_TOOL_METHOD_NAME)
        .description("Import tool method. Valid values are: " + StringUtils.join(validImportToolMethods, ","))
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(validImportToolMethods)
        .defaultValue(DEFAULT_IMPORT_TOOL_METHOD)
        .build();

    public static final PropertyDescriptor IMPORT_TOOL_JOB_TYPE = new PropertyDescriptor.Builder()
        .name(IMPORT_TOOL_JOB_TYPE_NAME)
        .description("Import tool job type. Valid values are: " + StringUtils.join(validImportToolJobTypes, ","))
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(validImportToolJobTypes)
        .defaultValue(DEFAULT_IMPORT_TOOL_JOB_TYPE)
        .build();

    public static final PropertyDescriptor IMPORT_TOOL_FILEFORMAT = new PropertyDescriptor.Builder()
        .name(IMPORT_TOOL_FILEFORMAT_NAME)
        .description("Import tool file format. Valid values are: " + StringUtils.join(validImportToolFileFormats, ","))
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(validImportToolFileFormats)
        .defaultValue(DEFAULT_IMPORT_TOOL_FILEFORMAT)
        .build();

    public static final PropertyDescriptor EXPORT_TOOL_METHOD = new PropertyDescriptor.Builder()
        .name(EXPORT_TOOL_METHOD_NAME)
        .description("Export tool method. Valid values are: " + StringUtils.join(validExportToolMethods, ","))
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(validExportToolMethods)
        .defaultValue(DEFAULT_EXPORT_TOOL_METHOD)
        .build();

    public static final PropertyDescriptor EXPORT_TOOL_JOB_TYPE = new PropertyDescriptor.Builder()
        .name(EXPORT_TOOL_JOB_TYPE_NAME)
        .description("Export tool job type. Valid values are: " + StringUtils.join(validExportToolJobTypes, ","))
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(validExportToolJobTypes)
        .defaultValue(DEFAULT_EXPORT_TOOL_JOB_TYPE)
        .build();

    public static final PropertyDescriptor EXPORT_TOOL_FILEFORMAT = new PropertyDescriptor.Builder()
        .name(EXPORT_TOOL_FILEFORMAT_NAME)
        .description("Export tool file format. Valid values are: " + StringUtils.join(validExportToolFileFormats, ","))
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(validExportToolFileFormats)
        .defaultValue(DEFAULT_EXPORT_TOOL_FILEFORMAT)
        .build();

    public static final PropertyDescriptor NUMBER_OF_MAPPERS = new PropertyDescriptor.Builder()
        .name(NUMBER_OF_MAPPERS_NAME)
        .description("The number of mappers used by the TDCH job. "
                     + "It is also the number of splits TDCH will attempt to create when utilizing a Teradata source plugin. "
                     + "This value is only a recommendation to the MR framework, and the framework may or may not spawn the exact amount of mappers requested by the user.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .defaultValue(DEFAULT_NUMBER_OF_MAPPERS)
        .build();

    public static final PropertyDescriptor THROTTLE_MAPPERS_FLAG = new PropertyDescriptor.Builder()
        .name(THROTTLE_MAPPERS_FLAG_NAME)
        .description("Force the TDCH job to only use as many mappers as the queue associated with the job can handle concurrently, "
                     + "overwriting the user defined '" + NUMBER_OF_MAPPERS_NAME + "' value.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue(DEFAULT_THROTTLE_MAPPERS_FLAG)
        .build();

    public static final PropertyDescriptor MINIMUM_MAPPERS = new PropertyDescriptor.Builder()
        .name(MINIMUM_MAPPERS_NAME)
        .description("Overwrite the user defined '" + NUMBER_OF_MAPPERS_NAME + "' value if/when the queue associated with the job has >= '" + MINIMUM_MAPPERS_NAME
                     + "' concurrently available. "
                     + "This property is only applied when the '" + THROTTLE_MAPPERS_FLAG_NAME + "' property is enabled.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .build();

    public static final PropertyDescriptor SOURCE_DATE_FORMAT = new PropertyDescriptor.Builder()
        .name(SOURCE_DATE_FORMAT_NAME)
        .description("The parse pattern to apply to all input string columns during conversion to the output column type, "
                     + "where the output column type is determined to be a date column.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_SOURCE_DATE_FORMAT)
        .build();

    public static final PropertyDescriptor SOURCE_TIME_FORMAT = new PropertyDescriptor.Builder()
        .name(SOURCE_TIME_FORMAT_NAME)
        .description("The parse pattern to apply to all input string columns during conversion to the output column type, "
                     + "where the output column type is determined to be a time column.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_SOURCE_TIME_FORMAT)
        .build();

    public static final PropertyDescriptor SOURCE_TIMESTAMP_FORMAT = new PropertyDescriptor.Builder()
        .name(SOURCE_TIMESTAMP_FORMAT_NAME)
        .description("The parse pattern to apply to all input string columns during conversion to the output column type, "
                     + "where the output column type is determined to be a timestamp column.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_SOURCE_TIMESTAMP_FORMAT)
        .build();

    public static final PropertyDescriptor SOURCE_TIMEZONE_ID = new PropertyDescriptor.Builder()
        .name(SOURCE_TIMEZONE_ID_NAME)
        .description("The source timezone used during conversions to or from date and time types. "
                     + "By default, the Hadoop cluster's default timezone will be used.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TARGET_DATE_FORMAT = new PropertyDescriptor.Builder()
        .name(TARGET_DATE_FORMAT_NAME)
        .description("The format of all output string columns, when the input column type is determined to be a date column.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_TARGET_DATE_FORMAT)
        .build();

    public static final PropertyDescriptor TARGET_TIME_FORMAT = new PropertyDescriptor.Builder()
        .name(TARGET_TIME_FORMAT_NAME)
        .description("The format of all output string columns, when the input column type is determined to be a time column.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_TARGET_TIME_FORMAT)
        .build();

    public static final PropertyDescriptor TARGET_TIMESTAMP_FORMAT = new PropertyDescriptor.Builder()
        .name(TARGET_TIMESTAMP_FORMAT_NAME)
        .description("The format of all output string columns, when the input column type is determined to be a timestamp column.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue(DEFAULT_TARGET_TIMESTAMP_FORMAT)
        .build();

    public static final PropertyDescriptor TARGET_TIMEZONE_ID = new PropertyDescriptor.Builder()
        .name(TARGET_TIMEZONE_ID_NAME)
        .description("The target timezone used during conversions to or from date and time types. "
                     + "By default, the Hadoop cluster's default timezone will be used.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor STRING_TRUNCATE_FLAG = new PropertyDescriptor.Builder()
        .name(STRING_TRUNCATE_FLAG_NAME)
        .description("If unspecified, default is 'true'. If set to 'true', strings will be silently truncated based on the length of the target char or varchar column. "
                     + "If set to 'false', when a string is larger than the target column an exception will be thrown and the mapper will fail.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue(DEFAULT_STRING_TRUNCATE_FLAG)
        .build();

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {

    }

    /**
     * Called by the framework, this method does additional validation on properties
     *
     * @param validationContext used to retrieves the properties to check
     * @return A collection of {@link ValidationResult} which will be checked by the framework
     */
    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        final List<ValidationResult> results = new ArrayList<>();
        final Boolean throttleMappersFlag = Boolean.valueOf(validationContext.getProperty(THROTTLE_MAPPERS_FLAG).evaluateAttributeExpressions().getValue());
        String minimumMappersAsString = validationContext.getProperty(MINIMUM_MAPPERS).evaluateAttributeExpressions().getValue();

        if (minimumMappersAsString != null && !minimumMappersAsString.isEmpty()) {
            final Integer minimumMappers = Integer.valueOf(validationContext.getProperty(MINIMUM_MAPPERS).evaluateAttributeExpressions().getValue());
            if ((minimumMappers > 0) && (!throttleMappersFlag)) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("'" + MINIMUM_MAPPERS_NAME + "' property requires '" + THROTTLE_MAPPERS_FLAG_NAME + "' to be enabled (set to true)")
                                .build());
            }
        } else if (throttleMappersFlag && minimumMappersAsString == null) {
            results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation("Setting property '" + THROTTLE_MAPPERS_FLAG_NAME + "' to true requires providing value for '" + MINIMUM_MAPPERS_NAME + "'")
                            .build());
        }

        return results;
    }
}
