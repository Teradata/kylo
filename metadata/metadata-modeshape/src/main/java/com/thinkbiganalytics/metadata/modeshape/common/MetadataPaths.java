/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.nio.file.Path;

/**
 * Defines root metadata paths within the JCR repository.
 */
public interface MetadataPaths {

    Path METADATA = JcrUtil.path("metadata");
    Path CATALOG = METADATA.resolve("catalog");
    Path CONNECTORS = CATALOG.resolve("connectors");
    Path FEEDS = METADATA.resolve("feeds");
    Path TEMPLATES = METADATA.resolve("templates");
    Path SLA = METADATA.resolve("sla");
    Path DOMAIN_TYPES = METADATA.resolve("domainTypes");
    
    static Path categoryPath(String systemName) {
        return FEEDS.resolve(systemName);
    }
    
    static Path feedPath(String categorySystemName, String feedSystemName) {
        return categoryPath(categorySystemName).resolve("tba:detals").resolve(feedSystemName);
    }
    
    static Path connectorPath(String systemName) {
        return CONNECTORS.resolve(systemName);
    }
    
    static Path dataSourcesPath(String connSystemName) {
        return connectorPath(connSystemName).resolve("dataSources");
    }
    
    static Path dataSourcePath(String connSystemName, String dsSystemName) {
        return dataSourcesPath(connSystemName).resolve(dsSystemName);
    }
    
    static Path dataSetsPath(String connSystemName, String dsSystemName) {
        return dataSourcesPath(connSystemName).resolve(dsSystemName).resolve("dataSets");
    }
    
    static Path dataSetPath(String connSystemName, String dsSystemName, String dataSetSystemName) {
        return dataSetsPath(connSystemName, dsSystemName).resolve(dataSetSystemName);
    }
}
