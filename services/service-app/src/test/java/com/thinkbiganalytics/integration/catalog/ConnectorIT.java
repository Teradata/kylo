package com.thinkbiganalytics.integration.catalog;

/*-
 * #%L
 * kylo-service-app
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

import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.kylo.catalog.rest.controller.ConnectorController;
import com.thinkbiganalytics.kylo.catalog.rest.model.Connector;

import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ConnectorIT extends IntegrationTestBase {

    /**
     * Verify retrieving connectors.
     */
    @Test
    public void testConnectors() {
        final Connector[] connectors = given(ConnectorController.BASE)
            .when().get()
            .then().statusCode(200)
            .extract().as(Connector[].class);

        final Matcher<Connector> isFileUpload = new CustomMatcher<Connector>("is file upload connector") {
            @Override
            public boolean matches(Object item) {
                return (item instanceof Connector && "File Upload".equals(((Connector) item).getTitle()));
            }
        };
        Assert.assertThat(Arrays.asList(connectors), CoreMatchers.hasItem(isFileUpload));
    }

    @Override
    protected void cleanup() {
        // nothing to do
    }
}
