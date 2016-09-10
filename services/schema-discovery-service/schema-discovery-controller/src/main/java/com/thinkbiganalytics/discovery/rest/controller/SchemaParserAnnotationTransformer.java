/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.rest.controller;

import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.policy.BasePolicyAnnotationTransformer;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;

import java.util.List;

/**
 * Transforms the schema parser UI model to/from
 */
public class SchemaParserAnnotationTransformer extends BasePolicyAnnotationTransformer<SchemaParserDescriptor, FileSchemaParser, SchemaParser> {

    @Override
    public SchemaParserDescriptor buildUiModel(SchemaParser annotation, FileSchemaParser policy, List<FieldRuleProperty> properties) {
        SchemaParserDescriptor descriptor = new SchemaParserDescriptor();
        descriptor.setProperties(properties);
        descriptor.setName(annotation.name());
        descriptor.setDescription(annotation.description());
        descriptor.setProperties(properties);
        descriptor.setObjectClassType(policy.getClass().getTypeName());
        return descriptor;
    }

    @Override
    public Class<SchemaParser> getAnnotationClass() {
        return SchemaParser.class;
    }
}

