package com.thinkbiganalytics.nifi.v1.logging;

/*-
 * #%L
 * thinkbig-nifi-framework-v1
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

import com.thinkbiganalytics.nifi.logging.ComponentLogFactory;

import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link ComponentLogFactory} for NiFi v1.0.
 */
@Component
public class ComponentLogFactoryV1 implements ComponentLogFactory {

    @Nonnull
    @Override
    public ComponentLog getLog(@Nonnull ProcessorInitializationContext context) {
        return context.getLogger();
    }
}
