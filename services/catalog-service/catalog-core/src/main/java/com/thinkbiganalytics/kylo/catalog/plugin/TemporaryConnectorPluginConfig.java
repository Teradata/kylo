/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.plugin;

/*-
 * #%L
 * kylo-catalog-core
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.kylo.catalog.ConnectorPluginManager;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginDescriptor;
import com.thinkbiganalytics.kylo.catalog.rest.model.UiOption;
import com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Named;

/**
 * This is a temporary plugin configuration that generates multiple plugins from a single
 * JSON config file.  This should be replaced with actual Kylo plugin modules that define
 * individual connectors.
 */
@Configuration
public class TemporaryConnectorPluginConfig {
    
    private static final TypeReference<List<ConnectorPluginDescriptor>> DESCR_TYPE = new TypeReference<List<ConnectorPluginDescriptor>>() { };
    
    @Bean(name="tempConnectorPluginResource")
    public Resource tempConnectorPluginConfigResource() {
        return new ClassPathResource("catalog-connector-plugins.json");
    }
    
    @Bean
    public ConnectorPluginManager connectorPluginManager(Optional<List<ConnectorPlugin>> plugins) {
        List<ConnectorPlugin> list = plugins.orElse(Collections.emptyList());
        ConnectorPluginManager mgr = new DefaultConnectorPluginManager();
        list.forEach(plugin -> mgr.register(plugin));
        return mgr;
    }

    @Bean
    public PluginBeanGenerator pluginBeanGenerator(@Named("tempConnectorPluginResource") Resource configResource) throws IOException {
        final String connectionsJson = IOUtils.toString(configResource.getInputStream(), StandardCharsets.UTF_8);
        final List<ConnectorPluginDescriptor> descrs = ObjectMapperSerializer.deserialize(connectionsJson, DESCR_TYPE);
        
        return new PluginBeanGenerator(descrs);
    }
    
    
    /**
     * A plugin that simply wraps a descriptor.
     */
    public static class SimpleConnectorPlugin implements ConnectorPlugin {
        
        private final ConnectorPluginDescriptor descriptor;

        public SimpleConnectorPlugin(ConnectorPluginDescriptor descriptor) {
            this.descriptor = new ConnectorPluginDescriptor(descriptor);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin#getId()
         */
        @Override
        public String getId() {
            return this.descriptor.getPluginId();
        }
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin#getDescriptor()
         */
        @Override
        public ConnectorPluginDescriptor getDescriptor() {
            return this.descriptor;
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin#getVersion()
         */
        @Override
        public String getVersion() {
            return "0.0";
        }
        
        /* (non-Javadoc)
         * @see com.thinkbiganalytics.kylo.catalog.spi.ConnectorPlugin#isSensitiveOption(java.lang.String)
         */
        @Override
        public boolean isSensitiveOption(String name) {
            return getDescriptor().getOptions().stream()
                .filter(option -> option.getKey().equals(name))
                .map(UiOption::isSensitive)
                .findFirst()
                .orElse(false);
        }
    }
    
    /**
     * A post processor that dynamically generates ConnectorPlugin beans based on the 
     * the list of descriptors passed to its constructor.
     */
    public static class PluginBeanGenerator implements BeanFactoryPostProcessor {
        
        private List<ConnectorPluginDescriptor> descriptors = new ArrayList<>();

        public PluginBeanGenerator(List<ConnectorPluginDescriptor> descriptors) {
            this.descriptors.addAll(descriptors);
        }

        /* (non-Javadoc)
         * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
         */
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            
            for (ConnectorPluginDescriptor descr : this.descriptors) {
                BeanDefinition beanDefinition = BeanDefinitionBuilder
                                .genericBeanDefinition(SimpleConnectorPlugin.class)
                                .addConstructorArgValue(descr)  
                                .getBeanDefinition();
                
                registry.registerBeanDefinition(descr.getPluginId() + "ConnectorPlugin", beanDefinition);
            }
        }
    }
}
