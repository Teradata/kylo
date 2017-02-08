package com.thinkbiganalytics.feedmgr.rest.model.schema;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class TableSetupTest {

    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        FeedMetadata feedMetadata = new FeedMetadata();
        feedMetadata.setCategory(new FeedCategory());
        feedMetadata.setTable(new TableSetup());
        feedMetadata.getTable().setTableSchema(new DefaultTableSchema());
        feedMetadata.getTable().getTableSchema().setName("test");
        DefaultField f1 = new DefaultField();
        f1.setName("field1");
        feedMetadata.getTable().getTableSchema().getFields().add(f1);

        String json = mapper.writeValueAsString(feedMetadata);
        FeedMetadata feedMetadata2 = mapper.readValue(json, FeedMetadata.class);
        assertEquals(feedMetadata2.getTable().getTableSchema().getName(), feedMetadata.getTable().getTableSchema().getName());

    }

}
