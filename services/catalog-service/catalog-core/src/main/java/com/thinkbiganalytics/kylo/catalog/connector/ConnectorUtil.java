package com.thinkbiganalytics.kylo.catalog.connector;

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

import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorPluginDescriptor;
import com.thinkbiganalytics.kylo.catalog.rest.model.ConnectorTab;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Static utility methods for {@link Connector} instances.
 */
public class ConnectorUtil {

    /**
     * Indicates if the connector has a tab matching any sref.
     */
    public static boolean hasAnyTabSref(@Nonnull final ConnectorPluginDescriptor descr, @Nonnull final List<String> srefs) {
        return srefs.stream().anyMatch(sref -> hasTabSref(descr, sref));
    }

    /**
     * Indicates if the connector has any tab with a matching sref.
     */
    public static boolean hasTabSref(@Nonnull final ConnectorPluginDescriptor descr, @Nonnull final String sref) {
        return (descr.getTabs() != null && descr.getTabs().stream().map(ConnectorTab::getSref).anyMatch(sref::equals));
    }

    /**
     * Instances of {@code ConnectorUtil} should not be constructed.
     */
    private ConnectorUtil() {
        throw new UnsupportedOperationException();
    }
}
