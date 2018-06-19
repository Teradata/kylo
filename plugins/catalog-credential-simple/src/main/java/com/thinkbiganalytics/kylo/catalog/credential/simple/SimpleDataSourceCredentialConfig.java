/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.simple;

/*-
 * #%L
 * kylo-catalog-credential-simple
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

import com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import javax.inject.Named;

/**
 *
 */
@Configuration
public class SimpleDataSourceCredentialConfig {
    
    @Bean
    public Resource simpleCredentialConfigResource() {
        return new ClassPathResource("credential-config-simple.json");
    }

    @Bean
    public DataSourceCredentialProvider simpleDataSourceCredentialProvider(@Named("simpleCredentialConfigResource") Resource configResource) throws IOException {
        SimpleDataSourceCredentialProvider provider = new SimpleDataSourceCredentialProvider();
        provider.loadCredentials(configResource);
        return provider;
    }
}
