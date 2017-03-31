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

import com.thinkbiganalytics.metadata.api.datasource.UserDatasource;

import javax.annotation.Nonnull;
import javax.jcr.Node;

/**
 * A {@code UserDatasource} that is stored in JRC.
 */
public abstract class JcrUserDatasource extends JcrDatasource implements UserDatasource {

    /**
     * Sub-node under {@code datasources} node for {@code userDatasource} nodes
     */
    @SuppressWarnings("unused")
    public static String PATH_NAME = "user";

    /**
     * Name of the type attribute
     */
    private static final String TYPE = "tba:type";

    /**
     * Constructs a {@code JcrUserDatasource} with the specified JCR node.
     *
     * @param node the JCR node
     */
    public JcrUserDatasource(@Nonnull final Node node) {
        super(node);
    }

    @Override
    public void setName(@Nonnull final String name) {
        setProperty(TITLE, name);
    }

    @Nonnull
    @Override
    public String getType() {
        return getProperty(TYPE, String.class);
    }

    @Override
    public void setType(@Nonnull final String type) {
        setProperty(TYPE, type);
    }
}
