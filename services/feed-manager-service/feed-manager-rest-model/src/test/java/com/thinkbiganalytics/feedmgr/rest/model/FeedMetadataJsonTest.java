package com.thinkbiganalytics.feedmgr.rest.model;

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

import com.thinkbiganalytics.json.ObjectMapperSerializer;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
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
        try (InputStream inputStream = r.getInputStream()){
            String json = IOUtils.toString(inputStream, Charset.defaultCharset());
            FeedMetadata feed = ObjectMapperSerializer.deserialize(json, FeedMetadata.class);
            assertNotNull(feed);
        }

    }

}
