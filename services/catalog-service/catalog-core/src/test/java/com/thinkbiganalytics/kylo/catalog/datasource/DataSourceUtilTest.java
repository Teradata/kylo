package com.thinkbiganalytics.kylo.catalog.datasource;

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
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class DataSourceUtilTest {

    /**
     * Verify merging paths for a data source.
     */
    @Test
    public void getPaths() {
        // Create mock connector
        final DefaultDataSetTemplate connectorTemplate = new DefaultDataSetTemplate();
        connectorTemplate.setOptions(Collections.singletonMap("path", "connector1.txt"));
        connectorTemplate.setPaths(Collections.singletonList("connector2.txt"));

        final Connector connector = new Connector();
        connector.setTemplate(connectorTemplate);

        // Create mock data source
        final DefaultDataSetTemplate dataSourceTemplate = new DefaultDataSetTemplate();
        dataSourceTemplate.setOptions(Collections.singletonMap("path", "datasource1.txt"));
        dataSourceTemplate.setPaths(Collections.singletonList("datasource2.txt"));

        final DataSource dataSource = new DataSource();
        dataSource.setConnector(connector);
        dataSource.setTemplate(dataSourceTemplate);

        // Test retrieving data source paths
        Assert.assertEquals(Arrays.asList("datasource1.txt", "datasource2.txt"), DataSourceUtil.getPaths(dataSource).orElse(null));

        // Test retrieving connector paths
        dataSourceTemplate.setOptions(null);
        Assert.assertEquals(Arrays.asList("connector1.txt", "datasource2.txt"), DataSourceUtil.getPaths(dataSource).orElse(null));

        dataSourceTemplate.setPaths(null);
        Assert.assertEquals(Arrays.asList("connector1.txt", "connector2.txt"), DataSourceUtil.getPaths(dataSource).orElse(null));

        // Test retrieving empty paths
        connectorTemplate.setOptions(null);
        connectorTemplate.setPaths(null);
        Assert.assertEquals(Optional.empty(), DataSourceUtil.getPaths(dataSource));
    }
}
