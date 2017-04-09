package com.thinkbiganalytics.metadata.modeshape.datasource;

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

import com.thinkbiganalytics.metadata.api.datasource.DatasourceDetails;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;

import javax.annotation.Nonnull;
import javax.jcr.Node;

/**
 * A Data Source Details node stored in JCR.
 */
public abstract class JcrDatasourceDetails extends JcrPropertiesEntity implements DatasourceDetails {

    /**
     * Constructs a {@code JcrDatasourceDetails} with the specified JCR node.
     *
     * @param node the JCR node
     */
    public JcrDatasourceDetails(@Nonnull final Node node) {
        super(node);
    }
}
