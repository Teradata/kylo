package com.thinkbiganalytics.nifi.v2.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.ingest.TableRegisterSupport;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.TableType;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * A NiFi processor that drops the Hive tables specified in the properties.
 */
@CapabilityDescription("Drops the standard feed tables managed by the Think Big platform.")
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hive", "ddl", "drop", "thinkbig"})
public class DropFeedTables extends AbstractNiFiProcessor {

    /**
     * Property specifying additional tables to drop
     */
    public static final PropertyDescriptor ADDITIONAL_TABLES = new PropertyDescriptor.Builder()
        .name("Additional Tables")
        .description("Additional tables to drop separated by comma.")
        .expressionLanguageSupported(true)
        .required(false)
        .addValidator(Validator.VALID)  // required for unit tests, see NIFI-1977
        .build();

    /**
     * Value indicating all tables should be dropped
     */
    public static final String ALL_TABLES = "ALL";

    /**
     * Property indicating which tables to drop
     */
    public static final PropertyDescriptor TABLE_TYPE = new PropertyDescriptor.Builder()
        .name("Table Type")
        .description("Specifies the standard table type to drop or ALL for standard set.")
        .allowableValues(Stream.concat(Arrays.stream(TableType.values()).map(Enum::toString), Stream.of(ALL_TABLES)).collect(Collectors.toSet()))
        .required(true)
        .build();

    /**
     * Configuration fields
     */
    private static final List<PropertyDescriptor> properties = ImmutableList.of(IngestProperties.THRIFT_SERVICE, IngestProperties.FEED_CATEGORY, IngestProperties.FEED_NAME, TABLE_TYPE,
                                                                                ADDITIONAL_TABLES);

    /**
     * Output paths to other NiFi processors
     */
    private static final Set<Relationship> relationships = ImmutableSet.of(IngestProperties.REL_SUCCESS, IngestProperties.REL_FAILURE);

    @Nonnull
    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Nonnull
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public void onTrigger(@Nonnull final ProcessContext context, @Nonnull final ProcessSession session) throws ProcessException {
        // Verify flow file exists
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        // Verify properties and attributes
        String additionalTablesValue = context.getProperty(ADDITIONAL_TABLES).evaluateAttributeExpressions(flowFile).getValue();
        Set<String> additionalTables = (additionalTablesValue != null) ? ImmutableSet.copyOf(additionalTablesValue.split(",")) : ImmutableSet.of();

        String entity = context.getProperty(IngestProperties.FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
        if (entity == null || entity.isEmpty()) {
            getLog().error("Missing feed name");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        String source = context.getProperty(IngestProperties.FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
        if (source == null || source.isEmpty()) {
            getLog().error("Missing category name");
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
            return;
        }

        Set<TableType> tableTypes;
        String tableTypesValue = context.getProperty(TABLE_TYPE).getValue();
        if (ALL_TABLES.equals(tableTypesValue)) {
            tableTypes = ImmutableSet.copyOf(TableType.values());
        } else {
            tableTypes = ImmutableSet.of(TableType.valueOf(tableTypesValue));
        }

        // Drop the tables
        final ThriftService thriftService = context.getProperty(IngestProperties.THRIFT_SERVICE).asControllerService(ThriftService.class);

        try (final Connection conn = thriftService.getConnection()) {
            boolean result = new TableRegisterSupport(conn).dropTables(source, entity, tableTypes, additionalTables);
            session.transfer(flowFile, result ? IngestProperties.REL_SUCCESS : IngestProperties.REL_FAILURE);
        } catch (final Exception e) {
            getLog().error("Unable drop tables", e);
            session.transfer(flowFile, IngestProperties.REL_FAILURE);
        }
    }
}
