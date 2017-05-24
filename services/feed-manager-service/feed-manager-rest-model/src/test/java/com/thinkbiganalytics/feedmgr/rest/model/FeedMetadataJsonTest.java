package com.thinkbiganalytics.feedmgr.rest.model;

import com.thinkbiganalytics.json.ObjectMapperSerializer;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;

import static org.junit.Assert.assertNotNull;

/**
 *Test Feed deserialization
 */
public class FeedMetadataJsonTest {


    @Test
    public void deserializationSortedTest() throws Exception{

        Resource r = new ClassPathResource("sorted-feed.json");
        String json = IOUtils.toString(r.getInputStream(), Charset.defaultCharset());
        FeedMetadata feed = ObjectMapperSerializer.deserialize(json,FeedMetadata.class);
        assertNotNull(feed);
    }

    @Test
    public void deserializationUnsortedTest() throws Exception{

        Resource r = new ClassPathResource("unsorted-feed.json");
        String json = IOUtils.toString(r.getInputStream(), Charset.defaultCharset());
        FeedMetadata feed = ObjectMapperSerializer.deserialize(json,FeedMetadata.class);
        assertNotNull(feed);

    }

}
