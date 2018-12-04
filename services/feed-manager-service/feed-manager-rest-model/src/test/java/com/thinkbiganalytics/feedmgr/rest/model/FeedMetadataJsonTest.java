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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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

    @Test
    public void testBeanUtils(){
        FeedMetadata m = new FeedMetadata();
        m.setDataTransformation( new FeedDataTransformation());
        List<String> list = new ArrayList<>();
        list.add(UUID.randomUUID().toString());
        list.add(UUID.randomUUID().toString());
        String str = list.toString();
        m.getDataTransformation().setCatalogDataSourceIds(list);
        Object obj = null;
        Object obj2 = null;
        try {
            obj = org.apache.commons.beanutils.BeanUtils.getProperty(m, "dataTransformation.catalogDataSourceIds");
            obj2 = PropertyUtils.getProperty(m,"dataTransformation.catalogDataSourceIds");
        } catch (Exception e) {
            //    throw new RuntimeException(e);
        }
        if(obj != null){
            System.out.println(obj.toString());
        }
        if(obj2 != null){
           if(isCollectionOfStrings(obj2)){
               System.out.println(stringCollectionToString((Collection<String>)obj2));
           }
            System.out.println(obj2.toString());
        }

    }

    private String stringCollectionToString(Collection<String> collection){
        return collection.stream().collect(Collectors.joining(","));
    }

    private boolean isCollectionOfStrings(Object obj) {

        if (obj instanceof Collection) {
           return (boolean) ((Collection) obj).stream().findFirst().map(o -> {
                if (o instanceof String) {
                    return true;
                } else {
                    return false;
                }
            }).orElse(false);
        } else {
            return false;
        }
    }

}
