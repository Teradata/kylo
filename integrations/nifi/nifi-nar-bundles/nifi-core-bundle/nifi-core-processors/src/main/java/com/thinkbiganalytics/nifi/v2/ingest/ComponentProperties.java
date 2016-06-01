/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.ingest;

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

/**
 * Properties shared by ingest components
 */
public interface ComponentProperties {

    /**
     * Common Controller services
     **/

    PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Service")
        .description("Think Big metadata service")
        .required(true)
        .identifiesControllerService(MetadataProviderService.class)
        .build();

    PropertyDescriptor THRIFT_SERVICE = new PropertyDescriptor.Builder()
        .name("Database Connection Pooling Service")
        .description("The Controller Service that is used to obtain connection to database")
        .required(true)
        .identifiesControllerService(ThriftService.class)
        .build();

    /**
     * Common component properties
     **/

    PropertyDescriptor FEED_CATEGORY = new PropertyDescriptor.Builder()
        .name("System feed category")
        .description("System category of feed this processor supports")
        .required(true)
        .defaultValue("${metadata.category.systemName}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("System feed name")
        .description("Name of feed this processor supports")
        .defaultValue("${metadata.systemFeedName}")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor TARGET_TABLE = new PropertyDescriptor.Builder()
        .name("Target table")
        .description("Fully qualified name of the target table")
        .required(true)
        .defaultValue("${metadata.category.systemName}.${metadata.systemFeedName}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor FEED_PARTITION = new PropertyDescriptor.Builder()
        .name("Feed partition value")
        .description("Feed timestamp that identifies the current feed partition")
        .required(true)
        .defaultValue("${feedts}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor PARTITION_SPECIFICATION = new PropertyDescriptor.Builder()
        .name("Partition Specification")
        .description("Partition specification in format: field|type|formula\nfield|type|formula")
        .defaultValue("${metadata.table.partitionSpecs}")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor FIELD_SPECIFICATION = new PropertyDescriptor.Builder()
        .name("Field specification")
        .description("Pipe-delim format with the specifications for the fields (column name|data type|comment")
        .required(true)
        .defaultValue("${metadata.table.fieldStructure}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor FEED_FORMAT_SPECS = new PropertyDescriptor.Builder()
        .name("Feed Table Storage Format")
        .description(
            "Provide format and delimiter specification. This is the full clause starting with the INPUTFORMAT such as: INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ")
        .required(true)
        .defaultValue("${metadata.table.feedFormat}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor TARGET_FORMAT_SPECS = new PropertyDescriptor.Builder()
        .name("Target Table Format")
        .description(
            "Provide storage format specification for the target tables")
        .required(true)
        .defaultValue("{$metadata.targetFormat}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor TARGET_TBLPROPERTIES = new PropertyDescriptor.Builder()
        .name("Target Table Properties")
        .description("TblProperties clause generally specificying the compression option")
        .defaultValue("${metadata.table.targetTblProperties}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor PARTITION_SPECS = new PropertyDescriptor.Builder()
        .name("Partition specification")
        .description("Provide list of partition columns column-delimited")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .defaultValue("${metadata.table.partitionStructure}")
        .expressionLanguageSupported(true)
        .build();

    // Standard Relationships
    Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Successfully created FlowFile from .")
        .build();

    Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("SQL query execution failed. Incoming FlowFile will be penalized and routed to this relationship")
        .build();


}
