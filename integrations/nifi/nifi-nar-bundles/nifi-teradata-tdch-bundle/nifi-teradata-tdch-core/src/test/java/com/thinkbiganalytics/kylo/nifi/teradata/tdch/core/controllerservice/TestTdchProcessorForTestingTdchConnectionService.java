package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice;

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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;

import java.util.ArrayList;
import java.util.List;

/**
 * A test processor used for testing {@link StandardTdchConnectionService} via {@link StandardTdchConnectionServiceTest}
 */
public class TestTdchProcessorForTestingTdchConnectionService extends AbstractProcessor {

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {

    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
        propertyDescriptors.add(new PropertyDescriptor.Builder()
                                    .name("TDCH Connection Service Test Processor")
                                    .description("TDCH Connection Service Test Processor")
                                    .identifiesControllerService(TdchConnectionService.class)
                                    .required(true)
                                    .build());
        return propertyDescriptors;
    }
}
