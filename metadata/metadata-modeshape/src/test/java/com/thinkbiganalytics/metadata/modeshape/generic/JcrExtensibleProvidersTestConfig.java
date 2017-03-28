/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.generic;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import org.springframework.context.annotation.Bean;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleTypeProvider;


/**
 * Overrides the mock beans for use by JcrExtensibleProvidersTest.
 */
public class JcrExtensibleProvidersTestConfig {

    @Bean
    private ExtensibleTypeProvider typeProvider() {
        return new JcrExtensibleTypeProvider();
    }

    @Bean
    private ExtensibleEntityProvider entityProvider() {
        return new JcrExtensibleEntityProvider();
    }

    @Bean
    private JcrMetadataAccess metadata() {
        return new JcrMetadataAccess();
    }
}
