package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.main;

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
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.AbstractTdchProcessor;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchOperationType;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchProcessResult;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchProcessRunner;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils.TdchBuilder;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils.TdchUtils;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.common.KerberosConfig;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Kylo NiFi Processor to export data from Hive to Teradata via TDCH
 */
@Tags({"kylo", "thinkbig", "export", "tdch", "teradata", "rdbms", "database", "table"})
@CapabilityDescription("Export data from Hive to Teradata via TDCH")
@WritesAttributes({
                      @WritesAttribute(attribute = "tdch.export.hive.to.teradata.command", description = "TDCH command executed"),
                      @WritesAttribute(attribute = "tdch.export.hive.to.teradata.kylo.result.code", description = "Kylo result code for TDCH command execution"),
                      @WritesAttribute(attribute = "tdch.export.hive.to.teradata.input.record.count", description = "Number of records read from Hive"),
                      @WritesAttribute(attribute = "tdch.export.hive.to.teradata.output.record.count", description = "Number of records written to Teradata"),
                      @WritesAttribute(attribute = "tdch.export.hive.to.teradata.tdch.exit.code", description = "Exit code recorded by TDCH job"),
                      @WritesAttribute(attribute = "tdch.export.hive.to.teradata.tdch.time.taken", description = "Time taken by TDCH job")
                  })
public class TdchExportHiveToTeradata extends AbstractTdchProcessor {

    //Source (Hive) properties
    public static final String HIVE_CONFIGURATION_FILE_HDFS_PATH_NAME = "[Hive] Config file path in HDFS";
    public static final String HIVE_DATABASE_NAME = "[Hive] Database";
    public static final String HIVE_TABLE_NAME = "[Hive] Table";
    public static final String HIVE_FIELD_NAMES_NAME = "[Hive] Field names";
    public static final String HIVE_FIELD_SEPARATOR_NAME = "[Hive] Field separator";
    public static final String HIVE_LINE_SEPARATOR_NAME = "[Hive] Line separator";

    //Target (Teradata) properties
    public static final String TERADATA_DATABASE_TABLE_NAME = "[Teradata] Database.Table";
    public static final String TERADATA_FIELD_NAMES_NAME = "[Teradata] Field names";
    public static final String TERADATA_TRUNCATE_TABLE_NAME = "[Teradata] Truncate table?";
    public static final String TERADATA_USE_XVIEWS_NAME = "[Teradata] Use XViews?";
    public static final String TERADATA_QUERY_BAND_NAME = "[Teradata] Query band";
    public static final String TERADATA_BATCH_SIZE_NAME = "[Teradata] Batch size (rows)";
    public static final String TERADATA_STAGING_DATABASE_NAME = "[Teradata] Staging database";
    public static final String TERADATA_STAGING_TABLE_NAME = "[Teradata] Staging table";
    public static final String TERADATA_FORCE_STAGE_NAME = "[Teradata] Force staging?";
    public static final String TERADATA_KEEP_STAGE_TABLE_NAME = "[Teradata] Keep staging table?";
    public static final String TERADATA_FAST_LOAD_ERROR_TABLE_NAME = "[Teradata] Error table";
    public static final String TERADATA_FAST_LOAD_ERROR_DATABASE_NAME = "[Teradata] Error database";

    //Regular expressions
    private static final String HIVE_SITE_XML_REGEX = "[\\s\\S]*/hive-site.xml$";
    private static final String TERADATA_DATABASE_TABLE_REGEX = "[\\s\\S]+\\.[\\s\\S]+";
    private static final String TERADATA_QUERY_BAND_REGEX = "[\\s\\S]+=[\\s\\S]+[^=];$";

    //Default values
    private static final String DEFAULT_TERADATA_TRUNCATE_TABLE = "false";
    private static final String DEFAULT_TERADATA_USE_XVIEWS = "false";
    private static final String DEFAULT_TERADATA_BATCH_SIZE = "10000";
    private static final String DEFAULT_TERADATA_FORCE_STAGE = "false";
    private static final String DEFAULT_TERADATA_KEEP_STAGE = "false";

    //Processor Properties
    public static final PropertyDescriptor HIVE_CONFIGURATION_FILE_HDFS_PATH = new PropertyDescriptor.Builder()
        .name(HIVE_CONFIGURATION_FILE_HDFS_PATH_NAME)
        .description("The path to a Hive configuration file in HDFS. Path should end with '/hive-site.xml'"
                     + "The source Hive plugins can use this file for TDCH jobs launched through remote execution or on data nodes.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.createRegexMatchingValidator(Pattern.compile(HIVE_SITE_XML_REGEX)))
        .build();

    public static final PropertyDescriptor HIVE_DATABASE = new PropertyDescriptor.Builder()
        .name(HIVE_DATABASE_NAME)
        .description("The name of the database in Hive from which the source Hive plugins will read data.")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor HIVE_TABLE = new PropertyDescriptor.Builder()
        .name(HIVE_TABLE_NAME)
        .description("The name of the table in Hive from which the source Hive plugins will read data.")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor HIVE_FIELD_NAMES = new PropertyDescriptor.Builder()
        .name(HIVE_FIELD_NAMES_NAME)
        .description("The names of columns that the source Hive plugins will read from the source table in Hive. "
                     + "The value should be in comma separated format. "
                     + "The order of the source field names need to match the order of the target field names for schema mapping.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor HIVE_FIELD_SEPARATOR = new PropertyDescriptor.Builder()
        .name(HIVE_FIELD_SEPARATOR_NAME)
        .description("If unspecified, default is \\u0001. The field separator that the Hive textfile source plugin uses when reading from Hive delimited tables.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor HIVE_LINE_SEPARATOR = new PropertyDescriptor.Builder()
        .name(HIVE_LINE_SEPARATOR_NAME)
        .description("If unspecified, default is /\\n. The line separator that the Hive textfile source plugin uses when reading from Hive delimited tables.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TERADATA_DATABASE_TABLE = new PropertyDescriptor.Builder()
        .name(TERADATA_DATABASE_TABLE_NAME)
        .description("The name of the target database.table in the Teradata system where the target Teradata plugins will write data")
        .required(true)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.createRegexMatchingValidator(Pattern.compile(TERADATA_DATABASE_TABLE_REGEX)))
        .build();

    public static final PropertyDescriptor TERADATA_FIELD_NAMES = new PropertyDescriptor.Builder()
        .name(TERADATA_FIELD_NAMES_NAME)
        .description("The names of fields that the target Teradata plugins will write to the table in the Teradata system. "
                     + "The value should be in comma separated format. "
                     + "The order of the target field names must match the order of the source field names for schema mapping.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TERADATA_TRUNCATE_TABLE = new PropertyDescriptor.Builder()
        .name(TERADATA_TRUNCATE_TABLE_NAME)
        .description("If unspecified, default is false. If set to true, the Teradata target table will be truncated "
                     + "before data is transferred which results in an overwrite of the data.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue(DEFAULT_TERADATA_TRUNCATE_TABLE)
        .build();

    public static final PropertyDescriptor TERADATA_USE_XVIEWS = new PropertyDescriptor.Builder()
        .name(TERADATA_USE_XVIEWS_NAME)
        .description("If unspecified, default is false. If set to true, the target Teradata plugins will use XViews to get Teradata system information. "
                     + "This option allows users who have limited access privileges to run TDCH jobs, though performance may be degraded.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue(DEFAULT_TERADATA_USE_XVIEWS)
        .build();

    public static final PropertyDescriptor TERADATA_QUERY_BAND = new PropertyDescriptor.Builder()
        .name(TERADATA_QUERY_BAND_NAME)
        .description("A string(key=value;), when specified, is used to set the value of session level query band for the Teradata target plugins. An example of queryband is 'org=Finance;'")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.createRegexMatchingValidator(Pattern.compile(TERADATA_QUERY_BAND_REGEX)))
        .build();

    public static final PropertyDescriptor TERADATA_BATCH_SIZE = new PropertyDescriptor.Builder()
        .name(TERADATA_BATCH_SIZE_NAME)
        .description(
            "If unspecified, default is 10000. The number of rows the Teradata target plugins will attempt to batch before submitting the rows to the Teradata system, up to the 1 MB buffer size limit.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .defaultValue(DEFAULT_TERADATA_BATCH_SIZE)
        .build();

    public static final PropertyDescriptor TERADATA_STAGING_DATABASE = new PropertyDescriptor.Builder()
        .name(TERADATA_STAGING_DATABASE_NAME)
        .description("The database in the Teradata system in which the Teradata target plugins create the staging table, if a staging table is utilized. "
                     + "The value must be the name of an existing database in the Teradata system. "
                     + "The default value is the current logon database of the JDBC connection.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TERADATA_STAGING_TABLE = new PropertyDescriptor.Builder()
        .name(TERADATA_STAGING_TABLE_NAME)
        .description("The name of the staging table created by the Teradata target plugins, if a staging table is utilized. "
                     + "The staging table should not exist in the database. "
                     + "Default value: Table name from " + TERADATA_DATABASE_TABLE_NAME + " appended with a numerical time in the form ‘hhmmssSSS’, separated by an underscore.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TERADATA_FORCE_STAGE = new PropertyDescriptor.Builder()
        .name(TERADATA_FORCE_STAGE_NAME)
        .description("If unspecified, default is false. If set to true, then staging is used by the Teradata target plugins, irrespective of the target table’s definition.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue(DEFAULT_TERADATA_FORCE_STAGE)
        .build();

    public static final PropertyDescriptor TERADATA_KEEP_STAGE_TABLE = new PropertyDescriptor.Builder()
        .name(TERADATA_KEEP_STAGE_TABLE_NAME)
        .description(
            "If unspecified, default is false. If set to true, the staging table is not dropped by the Teradata target plugins when a failure occurs during the insert-select operation between the staging and target tables.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
        .defaultValue(DEFAULT_TERADATA_KEEP_STAGE)
        .build();

    public static final PropertyDescriptor TERADATA_FAST_LOAD_ERROR_DATABASE = new PropertyDescriptor.Builder()
        .name(TERADATA_FAST_LOAD_ERROR_DATABASE_NAME)
        .description("The name of the database where the error tables will be created by the internal.fastload Teradata target plugin. "
                     + "Default value: The current logon database of the JDBC connection")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TERADATA_FAST_LOAD_ERROR_TABLE = new PropertyDescriptor.Builder()
        .name(TERADATA_FAST_LOAD_ERROR_TABLE_NAME)
        .description("The prefix of the name of the error table created by the internal.fastload Teradata target plugin. "
                     + "Error tables are used by the FastLoad protocol to handle records with erroneous columns. "
                     + "Default value: The value of " + TERADATA_DATABASE_TABLE_NAME + " appended with the strings _ERR_1 and _ERR_2 for the first and second error tables, respectively.")
        .required(false)
        .sensitive(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    //Processor Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("[TDCH] Hive to Teradata export success")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("[TDCH] Hive to Teradata export failure")
        .build();

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    //Derived from common properties
    public static final PropertyDescriptor HIVE_EXPORT_TOOL_JOB_TYPE = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(EXPORT_TOOL_JOB_TYPE)
        .displayName("[Hive] " + EXPORT_TOOL_JOB_TYPE_NAME)
        .build();

    public static final PropertyDescriptor HIVE_EXPORT_TOOL_FILEFORMAT = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(EXPORT_TOOL_FILEFORMAT)
        .displayName("[Hive] " + EXPORT_TOOL_FILEFORMAT_NAME)
        .build();

    public static final PropertyDescriptor HIVE_SOURCE_DATE_FORMAT = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(SOURCE_DATE_FORMAT)
        .displayName("[Hive] " + SOURCE_DATE_FORMAT_NAME)
        .build();

    public static final PropertyDescriptor HIVE_SOURCE_TIME_FORMAT = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(SOURCE_TIME_FORMAT)
        .displayName("[Hive] " + SOURCE_TIME_FORMAT_NAME)
        .build();

    public static final PropertyDescriptor HIVE_SOURCE_TIMESTAMP_FORMAT = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(SOURCE_TIMESTAMP_FORMAT)
        .displayName("[Hive] " + SOURCE_TIMESTAMP_FORMAT_NAME)
        .build();

    public static final PropertyDescriptor HIVE_SOURCE_TIMEZONE_ID = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(SOURCE_TIMEZONE_ID)
        .displayName("[Hive] " + SOURCE_TIMEZONE_ID_NAME)
        .build();


    public static final PropertyDescriptor TERADATA_EXPORT_TOOL_METHOD = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(EXPORT_TOOL_METHOD)
        .displayName("[Teradata] " + EXPORT_TOOL_METHOD_NAME)
        .build();

    public static final PropertyDescriptor TERADATA_TARGET_DATE_FORMAT = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(TARGET_DATE_FORMAT)
        .displayName("[Teradata] " + TARGET_DATE_FORMAT_NAME)
        .build();

    public static final PropertyDescriptor TERADATA_TARGET_TIME_FORMAT = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(TARGET_TIME_FORMAT)
        .displayName("[Teradata] " + TARGET_TIME_FORMAT_NAME)
        .build();

    public static final PropertyDescriptor TERADATA_TARGET_TIMESTAMP_FORMAT = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(TARGET_TIMESTAMP_FORMAT)
        .displayName("[Teradata] " + TARGET_TIMESTAMP_FORMAT_NAME)
        .build();

    public static final PropertyDescriptor TERADATA_TARGET_TIMEZONE_ID = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(TARGET_TIMEZONE_ID)
        .displayName("[Teradata] " + TARGET_TIMEZONE_ID_NAME)
        .build();

    public static final PropertyDescriptor TERADATA_STRING_TRUNCATE_FLAG = new PropertyDescriptor.Builder()
        .fromPropertyDescriptor(STRING_TRUNCATE_FLAG)
        .displayName("[Teradata] " + STRING_TRUNCATE_FLAG_NAME)
        .build();

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        /* Create Kerberos properties */
        final SpringSecurityContextLoader securityContextLoader = SpringSecurityContextLoader.create(context);
        final KerberosProperties kerberosProperties = securityContextLoader.getKerberosProperties();
        KERBEROS_KEYTAB = kerberosProperties.createKerberosKeytabProperty();
        KERBEROS_PRINCIPAL = kerberosProperties.createKerberosPrincipalProperty();

        final List<PropertyDescriptor> properties = new ArrayList<>();

        //mandatory hive and teradata
        properties.add(HIVE_EXPORT_TOOL_JOB_TYPE);
        properties.add(HIVE_EXPORT_TOOL_FILEFORMAT);
        properties.add(HIVE_DATABASE);
        properties.add(HIVE_TABLE);
        properties.add(TDCH_CONNECTION_SERVICE);
        properties.add(TERADATA_EXPORT_TOOL_METHOD);
        properties.add(TERADATA_DATABASE_TABLE);

        //optional hive
        properties.add(HIVE_SOURCE_DATE_FORMAT);
        properties.add(HIVE_SOURCE_TIME_FORMAT);
        properties.add(HIVE_SOURCE_TIMESTAMP_FORMAT);
        properties.add(HIVE_SOURCE_TIMEZONE_ID);
        properties.add(HIVE_CONFIGURATION_FILE_HDFS_PATH);
        properties.add(HIVE_FIELD_NAMES);
        properties.add(HIVE_FIELD_SEPARATOR);
        properties.add(HIVE_LINE_SEPARATOR);

        //optional teradata
        properties.add(TERADATA_TARGET_DATE_FORMAT);
        properties.add(TERADATA_TARGET_TIME_FORMAT);
        properties.add(TERADATA_TARGET_TIMESTAMP_FORMAT);
        properties.add(TERADATA_TARGET_TIMEZONE_ID);
        properties.add(TERADATA_TRUNCATE_TABLE);
        properties.add(TERADATA_BATCH_SIZE);
        properties.add(TERADATA_STRING_TRUNCATE_FLAG);
        properties.add(TERADATA_USE_XVIEWS);
        properties.add(TERADATA_FIELD_NAMES);
        properties.add(TERADATA_QUERY_BAND);
        properties.add(TERADATA_STAGING_DATABASE);
        properties.add(TERADATA_STAGING_TABLE);
        properties.add(TERADATA_FORCE_STAGE);
        properties.add(TERADATA_KEEP_STAGE_TABLE);
        properties.add(TERADATA_FAST_LOAD_ERROR_DATABASE);
        properties.add(TERADATA_FAST_LOAD_ERROR_TABLE);

        //cluster
        properties.add(NUMBER_OF_MAPPERS);
        properties.add(THROTTLE_MAPPERS_FLAG);
        properties.add(MINIMUM_MAPPERS);

        //security
        properties.add(KERBEROS_PRINCIPAL);
        properties.add(KERBEROS_KEYTAB);

        this.properties = Collections.unmodifiableList(properties);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();

        if (flowFile == null) {
            flowFile = session.create();
            logger.info("[Hive to Teradata Export via TDCH] Created a flow file having uuid: {}", new Object[]{flowFile.getAttribute(CoreAttributes.UUID.key())});
        } else {
            logger.info("[Hive to Teradata Export via TDCH] Using an existing flow file having uuid: {}", new Object[]{flowFile.getAttribute(CoreAttributes.UUID.key())});
        }

        final String kerberosPrincipal = context.getProperty(KERBEROS_PRINCIPAL).getValue();
        final String kerberosKeyTab = context.getProperty(KERBEROS_KEYTAB).getValue();
        final TdchConnectionService tdchConnectionService = context.getProperty(TDCH_CONNECTION_SERVICE).asControllerService(TdchConnectionService.class);
        final String commonExportToolMethod = StringUtils.isEmpty(context.getProperty(EXPORT_TOOL_METHOD).evaluateAttributeExpressions(flowFile).getValue()) ?
                                              DEFAULT_EXPORT_TOOL_METHOD :
                                              context.getProperty(EXPORT_TOOL_METHOD).evaluateAttributeExpressions(flowFile).getValue();

        final Integer commonNumberOfMappers = StringUtils.isEmpty(context.getProperty(NUMBER_OF_MAPPERS).evaluateAttributeExpressions(flowFile).getValue()) ?
                                              Integer.valueOf(DEFAULT_NUMBER_OF_MAPPERS) :
                                              context.getProperty(NUMBER_OF_MAPPERS).evaluateAttributeExpressions(flowFile).asInteger();

        final Boolean commonThrottleMappersFlag = StringUtils.isEmpty(context.getProperty(THROTTLE_MAPPERS_FLAG).evaluateAttributeExpressions(flowFile).getValue()) ?
                                                  Boolean.valueOf(DEFAULT_THROTTLE_MAPPERS_FLAG) :
                                                  context.getProperty(THROTTLE_MAPPERS_FLAG).evaluateAttributeExpressions(flowFile).asBoolean();

        final Integer commonMinimumMappers = StringUtils.isEmpty(context.getProperty(MINIMUM_MAPPERS).evaluateAttributeExpressions(flowFile).getValue()) ?
                                             null :
                                             context.getProperty(MINIMUM_MAPPERS).evaluateAttributeExpressions(flowFile).asInteger();

        final String commonSourceDateFormat = context.getProperty(SOURCE_DATE_FORMAT).evaluateAttributeExpressions(flowFile).getValue();
        final String commonSourceTimeFormat = context.getProperty(SOURCE_TIME_FORMAT).evaluateAttributeExpressions(flowFile).getValue();
        final String commonSourceTimestampFormat = context.getProperty(SOURCE_TIMESTAMP_FORMAT).evaluateAttributeExpressions(flowFile).getValue();
        final String commonSourceTimezoneId = context.getProperty(SOURCE_TIMEZONE_ID).evaluateAttributeExpressions(flowFile).getValue();
        final String commonTargetDateFormat = context.getProperty(TARGET_DATE_FORMAT).evaluateAttributeExpressions(flowFile).getValue();
        final String commonTargetTimeFormat = context.getProperty(TARGET_TIME_FORMAT).evaluateAttributeExpressions(flowFile).getValue();
        final String commonTargetTimestampFormat = context.getProperty(TARGET_TIMESTAMP_FORMAT).evaluateAttributeExpressions(flowFile).getValue();
        final String commonTargetTimezoneId = context.getProperty(TARGET_TIMEZONE_ID).evaluateAttributeExpressions(flowFile).getValue();

        final Boolean commonStringTruncateFlag = StringUtils.isEmpty(context.getProperty(STRING_TRUNCATE_FLAG).evaluateAttributeExpressions(flowFile).getValue()) ?
                                                 Boolean.valueOf(DEFAULT_STRING_TRUNCATE_FLAG) :
                                                 context.getProperty(STRING_TRUNCATE_FLAG).evaluateAttributeExpressions(flowFile).asBoolean();

        final String commonExportToolJobType = StringUtils.isEmpty(context.getProperty(EXPORT_TOOL_JOB_TYPE).evaluateAttributeExpressions(flowFile).getValue()) ?
                                               DEFAULT_EXPORT_TOOL_JOB_TYPE :
                                               context.getProperty(EXPORT_TOOL_JOB_TYPE).evaluateAttributeExpressions(flowFile).getValue();

        final String commonExportToolFileFormat = StringUtils.isEmpty(context.getProperty(EXPORT_TOOL_FILEFORMAT).evaluateAttributeExpressions(flowFile).getValue()) ?
                                                  DEFAULT_EXPORT_TOOL_FILEFORMAT :
                                                  context.getProperty(EXPORT_TOOL_FILEFORMAT).evaluateAttributeExpressions(flowFile).getValue();

        final String hiveConfigurationFileHdfsPath = context.getProperty(HIVE_CONFIGURATION_FILE_HDFS_PATH).evaluateAttributeExpressions(flowFile).getValue();
        final String hiveDatabase = context.getProperty(HIVE_DATABASE).evaluateAttributeExpressions(flowFile).getValue();
        final String hiveTable = context.getProperty(HIVE_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        final String hiveFieldNames = context.getProperty(HIVE_FIELD_NAMES).evaluateAttributeExpressions(flowFile).getValue();
        final String hiveFieldSeparator = context.getProperty(HIVE_FIELD_SEPARATOR).evaluateAttributeExpressions(flowFile).getValue();
        final String hiveLineSeparator = context.getProperty(HIVE_LINE_SEPARATOR).evaluateAttributeExpressions(flowFile).getValue();
        final String teradataDatabaseTable = context.getProperty(TERADATA_DATABASE_TABLE).evaluateAttributeExpressions(flowFile).getValue();
        final String teradataFieldNames = context.getProperty(TERADATA_FIELD_NAMES).evaluateAttributeExpressions(flowFile).getValue();

        final Boolean teradataTruncateTable = StringUtils.isEmpty(context.getProperty(TERADATA_TRUNCATE_TABLE).evaluateAttributeExpressions(flowFile).getValue()) ?
                                              Boolean.valueOf(DEFAULT_TERADATA_TRUNCATE_TABLE) :
                                              context.getProperty(TERADATA_TRUNCATE_TABLE).evaluateAttributeExpressions(flowFile).asBoolean();

        final Boolean teradataUseXviews = StringUtils.isEmpty(context.getProperty(TERADATA_USE_XVIEWS).evaluateAttributeExpressions(flowFile).getValue()) ?
                                          Boolean.valueOf(DEFAULT_TERADATA_USE_XVIEWS) :
                                          context.getProperty(TERADATA_USE_XVIEWS).evaluateAttributeExpressions(flowFile).asBoolean();

        final String teradataQueryBand = context.getProperty(TERADATA_QUERY_BAND).evaluateAttributeExpressions(flowFile).getValue();

        final Integer teradataBatchSize = StringUtils.isEmpty(context.getProperty(TERADATA_BATCH_SIZE).evaluateAttributeExpressions(flowFile).getValue()) ?
                                          Integer.valueOf(DEFAULT_TERADATA_BATCH_SIZE) :
                                          context.getProperty(TERADATA_BATCH_SIZE).evaluateAttributeExpressions(flowFile).asInteger();

        final String teradataStagingDatabase = context.getProperty(TERADATA_STAGING_DATABASE).evaluateAttributeExpressions(flowFile).getValue();
        final String teradataStagingTable = context.getProperty(TERADATA_STAGING_TABLE).evaluateAttributeExpressions(flowFile).getValue();

        final Boolean teradataForceStage = StringUtils.isEmpty(context.getProperty(TERADATA_FORCE_STAGE).evaluateAttributeExpressions(flowFile).getValue()) ?
                                           Boolean.valueOf(DEFAULT_TERADATA_FORCE_STAGE) :
                                           context.getProperty(TERADATA_FORCE_STAGE).evaluateAttributeExpressions(flowFile).asBoolean();

        final Boolean teradataKeepStagingTable = StringUtils.isEmpty(context.getProperty(TERADATA_KEEP_STAGE_TABLE).evaluateAttributeExpressions(flowFile).getValue()) ?
                                                 Boolean.valueOf(DEFAULT_TERADATA_KEEP_STAGE) :
                                                 context.getProperty(TERADATA_KEEP_STAGE_TABLE).evaluateAttributeExpressions(flowFile).asBoolean();

        final String teradataFastLoadErrorDatabase = context.getProperty(TERADATA_FAST_LOAD_ERROR_DATABASE).evaluateAttributeExpressions(flowFile).getValue();
        final String teradataFastLoadErrorTable = context.getProperty(TERADATA_FAST_LOAD_ERROR_TABLE).evaluateAttributeExpressions(flowFile).getValue();

        final StopWatch stopWatch = new StopWatch(false);

        KerberosConfig kerberosConfig = new KerberosConfig()
            .setLogger(logger)
            .setKerberosPrincipal(kerberosPrincipal)
            .setKerberosKeytab(kerberosKeyTab);

        TdchBuilder tdchBuilder = new TdchBuilder();
        String tdchCommand = tdchBuilder
            .setLogger(logger)
            .setTdchJarEnvironmentVariable(TDCH_JAR_PATH_ENV_VAR_NAME)
            .setTdchLibraryJarsVariable(TDCH_LIB_JARS_ENV_VAR_NAME)
            .setTdchHadoopClassPathVariable(TDCH_HADOOP_CLASSPATH_ENV_VAR_NAME)
            .setTdchOperationType(TdchOperationType.TDCH_EXPORT)
            .setCommonTeradataUrl(tdchConnectionService.getJdbcConnectionUrl(), teradataDatabaseTable)
            .setCommonTeradataClassname(tdchConnectionService.getJdbcDriverClassName())
            .setCommonTeradataUsername(tdchConnectionService.getUserName())
            .setCommonTeradataPassword(tdchConnectionService.getPassword())
            .setCommonExportToolMethod(commonExportToolMethod)
            .setCommonExportToolJobType(commonExportToolJobType)
            .setCommonExportToolFileFormat(commonExportToolFileFormat)
            .setCommonNumMappers(commonNumberOfMappers)
            .setCommonThrottleMappers(commonThrottleMappersFlag)
            .setCommonMinMappers(commonMinimumMappers)
            .setCommonSourceDateFormat(commonSourceDateFormat)
            .setCommonSourceTimeFormat(commonSourceTimeFormat)
            .setCommonSourceTimestampFormat(commonSourceTimestampFormat)
            .setCommonSourceTimezoneId(commonSourceTimezoneId)
            .setCommonTargetDateFormat(commonTargetDateFormat)
            .setCommonTargetTimeFormat(commonTargetTimeFormat)
            .setCommonTargetTimestampFormat(commonTargetTimestampFormat)
            .setCommonTargetTimezoneId(commonTargetTimezoneId)
            .setCommonStringTruncate(commonStringTruncateFlag)
            .setSourceHiveConfigurationFileHdfsPath(hiveConfigurationFileHdfsPath)
            .setSourceHiveSourceDatabase(hiveDatabase)
            .setSourceHiveSourceTable(hiveTable)
            .setSourceHiveSourceFieldNames(hiveFieldNames)
            .setSourceHiveFieldSeparator(hiveFieldSeparator)
            .setSourceHiveLineSeparator(hiveLineSeparator)
            .setTargetTeradataDatabaseTable(teradataDatabaseTable)
            .setTargetTeradataTargetFieldNames(teradataFieldNames)
            .setTargetTeradataTruncateTable(teradataTruncateTable)
            .setTargetTeradataUseXviews(teradataUseXviews)
            .setTargetTeradataQueryBand(teradataQueryBand)
            .setTargetTeradataBatchSize(teradataBatchSize)
            .setTargetTeradataStagingDatabase(teradataStagingDatabase)
            .setTargetTeradataStagingTableName(teradataStagingTable)
            .setTargetTeradataForceStage(teradataForceStage)
            .setTargetTeradataKeepStageTable(teradataKeepStagingTable)
            .setTargetTeradataFastLoadErrorTableDatabase(teradataFastLoadErrorDatabase)
            .setTargetTeradataFastLoadErrorTableName(teradataFastLoadErrorTable)
            .build();

        List<String> tdchExecutionCommand = new ArrayList<>();
        tdchExecutionCommand.add(COMMAND_SHELL);
        tdchExecutionCommand.add(COMMAND_SHELL_FLAGS);
        tdchExecutionCommand.add(tdchCommand);

        Map<String, String> tdchEnvironmentVariables = new HashMap<>();
        tdchEnvironmentVariables.put(TDCH_JAR_PATH_ENV_VAR_NAME, tdchConnectionService.getTdchJarPath());
        tdchEnvironmentVariables.put(TDCH_LIB_JARS_ENV_VAR_NAME, tdchConnectionService.getTdchLibraryJarsPath());
        tdchEnvironmentVariables.put(TDCH_HADOOP_CLASSPATH_ENV_VAR_NAME, tdchConnectionService.getTdchHadoopClassPath());

        TdchProcessRunner tdchProcessRunner = new TdchProcessRunner(kerberosConfig,
                                                                    tdchExecutionCommand,
                                                                    logger,
                                                                    TdchOperationType.TDCH_EXPORT,
                                                                    tdchEnvironmentVariables);

        logger.info("Starting execution of TDCH command (Hive to Teradata export)");
        stopWatch.start();
        TdchProcessResult tdchProcessResult = tdchProcessRunner.execute();
        stopWatch.stop();
        logger.info("Finished execution of TDCH command (Hive to Teradata export)");

        int resultStatus = tdchProcessResult.getExitValue();

        TdchUtils tdchUtils = new TdchUtils();
        String tdchCommandWithCredentialsMasked = tdchUtils.maskTdchCredentials(tdchCommand);

        flowFile = session.putAttribute(flowFile, "tdch.export.hive.to.teradata.command", tdchCommandWithCredentialsMasked);
        flowFile = session.putAttribute(flowFile, "tdch.export.hive.to.teradata.kylo.result.code", String.valueOf(resultStatus));
        flowFile = session.putAttribute(flowFile, "tdch.export.hive.to.teradata.input.record.count", String.valueOf(tdchUtils.getExportHiveToTeradataInputRecordsCount(tdchProcessResult, logger)));
        flowFile = session.putAttribute(flowFile, "tdch.export.hive.to.teradata.output.record.count", String.valueOf(tdchUtils.getExportHiveToTeradataOutputRecordsCount(tdchProcessResult, logger)));
        flowFile = session.putAttribute(flowFile, "tdch.export.hive.to.teradata.tdch.exit.code", String.valueOf(tdchUtils.getExportHiveToTeradataJobExitCode(tdchProcessResult, logger)));
        flowFile = session.putAttribute(flowFile, "tdch.export.hive.to.teradata.tdch.time.taken", tdchUtils.getExportHiveToTeradataJobTimeTaken(tdchProcessResult, logger));
        logger.info("Wrote result attributes to flow file");

        if (resultStatus == 0) {
            logger.info("TDCH Hive to Teradata export OK [Code {}]", new Object[]{resultStatus});
            session.transfer(flowFile, REL_SUCCESS);
        } else {
            logger.error("TDCH Hive to Teradata export FAIL [Code {}]", new Object[]{resultStatus});
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    /**
     * Called by the framework, this method does additional validation on properties
     *
     * @param validationContext used to retrieves the properties to check
     * @return A collection of {@link ValidationResult} which will be checked by the framework
     */
    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        final Collection<ValidationResult> results = super.customValidate(validationContext);
        final String COMMA_DELIMITER = ",";
        final String hiveFieldNames = validationContext.getProperty(HIVE_FIELD_NAMES).evaluateAttributeExpressions().getValue();
        final String teradataFieldNames = validationContext.getProperty(TERADATA_FIELD_NAMES).evaluateAttributeExpressions().getValue();

        if (
            ((StringUtils.isNotEmpty(hiveFieldNames)) && (StringUtils.isEmpty(teradataFieldNames))) ||
            ((StringUtils.isEmpty(hiveFieldNames)) && (StringUtils.isNotEmpty(teradataFieldNames)))
            ) {
            results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation("Either both or none should be specified: " + HIVE_FIELD_NAMES_NAME + ", " + TERADATA_FIELD_NAMES_NAME)
                            .build());
        }

        if (StringUtils.countMatches(hiveFieldNames, COMMA_DELIMITER) != StringUtils.countMatches(teradataFieldNames, COMMA_DELIMITER)) {
            results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation(HIVE_FIELD_NAMES_NAME + " and " + TERADATA_FIELD_NAMES_NAME + " must specify the same number of comma-separated fields. "
                                         + "(Found " + StringUtils.countMatches(hiveFieldNames, COMMA_DELIMITER) + " and " + StringUtils.countMatches(teradataFieldNames, COMMA_DELIMITER) + ")")
                            .build());
        }

        return results;
    }
}
