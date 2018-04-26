package com.thinkbiganalytics.integration.domaintype;

/*-
 * #%L
 * kylo-service-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.feedmgr.rest.model.DomainType;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;

import org.junit.Assert;
import org.junit.Test;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Creates and updates a data source
 */
public class DomainTypeIT extends IntegrationTestBase {

    @Test
    public void testCreateAndUpdateDatasource() {
        DomainType[] initialDomainTypes = getDomainTypes();


        //create new domain type
        DomainType dt = new DomainType();
        dt.setTitle("Domain Type 1");
        dt.setDescription("domain type created by integration tests");

        DomainType response = createDomainType(dt);
        Assert.assertEquals(dt.getTitle(), response.getTitle());
        Assert.assertEquals(dt.getDescription(), response.getDescription());
        Assert.assertEquals(null, response.getIcon());
        Assert.assertEquals(null, response.getIconColor());


        //assert new domain type was added
        DomainType[] currentDomainTypes = getDomainTypes();
        Assert.assertEquals(initialDomainTypes.length + 1, currentDomainTypes.length);


        //update existing domain type
        dt = getDomainType(response.getId());
        dt.setTitle("Domain Type 1 with updated title");
        dt.setDescription("domain type description updated by integration tests");
        dt.setIcon("stars");
        dt.setIconColor("green");

        DomainType updated = createDomainType(dt);
        Assert.assertEquals(dt.getTitle(), updated.getTitle());
        Assert.assertEquals(dt.getDescription(), updated.getDescription());
        Assert.assertEquals("stars", updated.getIcon());
        Assert.assertEquals("green", updated.getIconColor());


        //assert domain type was updated, rather than added
        currentDomainTypes = getDomainTypes();
        Assert.assertEquals(initialDomainTypes.length + 1, currentDomainTypes.length);


        //delete domain type
        deleteDomainType(dt.getId());
        currentDomainTypes = getDomainTypes();
        Assert.assertEquals(initialDomainTypes.length , currentDomainTypes.length);

        //assert domain type was removed
        getDomainTypeExpectingStatus(dt.getId(), HTTP_NOT_FOUND);
    }
}
