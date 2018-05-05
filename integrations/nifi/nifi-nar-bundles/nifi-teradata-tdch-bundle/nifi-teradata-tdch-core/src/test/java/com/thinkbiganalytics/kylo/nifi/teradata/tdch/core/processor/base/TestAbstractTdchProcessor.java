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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessorInitializationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A test processor for use with {@link AbstractTdchProcessorTest}
 */
public class TestAbstractTdchProcessor extends AbstractTdchProcessor {

    private List<PropertyDescriptor> properties;

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(IMPORT_TOOL_METHOD);
        properties.add(IMPORT_TOOL_JOB_TYPE);
        properties.add(IMPORT_TOOL_FILEFORMAT);
        properties.add(EXPORT_TOOL_METHOD);
        properties.add(EXPORT_TOOL_JOB_TYPE);
        properties.add(EXPORT_TOOL_FILEFORMAT);
        properties.add(NUMBER_OF_MAPPERS);
        properties.add(THROTTLE_MAPPERS_FLAG);
        properties.add(MINIMUM_MAPPERS);
        properties.add(SOURCE_DATE_FORMAT);
        properties.add(SOURCE_TIME_FORMAT);
        properties.add(SOURCE_TIMESTAMP_FORMAT);
        properties.add(SOURCE_TIMEZONE_ID);
        properties.add(TARGET_DATE_FORMAT);
        properties.add(TARGET_TIME_FORMAT);
        properties.add(TARGET_TIMESTAMP_FORMAT);
        properties.add(TARGET_TIMEZONE_ID);
        properties.add(STRING_TRUNCATE_FLAG);
        this.properties = Collections.unmodifiableList(properties);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }
}
