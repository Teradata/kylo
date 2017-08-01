package com.thinkbiganalytics.metadata.modeshape.common;

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

import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Node;

/**
 * A collection of generic JSON properties.
 */
public class JcrGenericJsonProperties extends JcrObject {

    /**
     * Type name for this node.
     */
    public static final String NODE_TYPE = "tba:genericJsonProperties";

    /**
     * Constructs a {@code JcrGenericJsonProperties}.
     */
    public JcrGenericJsonProperties(@Nonnull final Node node) {
        super(node);
    }

    /**
     * Gets all generic JSON properties.
     */
    @Nonnull
    @Override
    public Map<String, Object> getProperties() {
        return JcrUtil.getNodesOfType(node, "tba:genericJson").stream()
            .collect(Collectors.toMap(JcrUtil::getName, JcrUtil::getGenericJson));
    }

    /**
     * Sets the specified generic JSON property.
     */
    public <T extends Serializable> void setProperty(@Nonnull final String name, @Nullable final T value) {
        JcrUtil.addGenericJson(node, name, value);
    }
}
