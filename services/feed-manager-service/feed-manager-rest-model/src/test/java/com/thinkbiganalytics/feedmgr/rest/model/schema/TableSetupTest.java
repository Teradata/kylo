/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by matthutton on 12/27/16.
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
        FeedMetadata feedMetadata2 = mapper.readValue(json,FeedMetadata.class);
        assertEquals(feedMetadata2.getTable().getTableSchema().getName(), feedMetadata.getTable().getTableSchema().getName());

    }

}