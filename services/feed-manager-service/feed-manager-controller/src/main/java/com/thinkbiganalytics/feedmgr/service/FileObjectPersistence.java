package com.thinkbiganalytics.feedmgr.service;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * persist the feed metadata to the file.
 * This should only be used in test cases and is not scalable for production use.
 */
public class FileObjectPersistence {

    private static String filePath = "/tmp";
    private static String FEED_METADATA_FILENAME = "feed-metadata2.json";
    private static String FEED_CATEGORIES_FILENAME = "feed-categories2.json";
    private static String TEMPLATE_METADATA_FILENAME = "registered-templates2.json";

    public static FileObjectPersistence getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void writeCategoriesToFile(Collection<FeedCategory> categories) {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath + "/" + FEED_CATEGORIES_FILENAME);
        try {
            mapper.writeValue(file, categories);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeFeedsToFile(Collection<FeedMetadata> feeds) {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath + "/" + FEED_METADATA_FILENAME);
        try {
            mapper.writeValue(file, feeds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeTemplatesToFile(Collection<RegisteredTemplate> templates) {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath + "/" + TEMPLATE_METADATA_FILENAME);
        try {
            mapper.writeValue(file, templates);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<FeedCategory> getCategoriesFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath + "/" + FEED_CATEGORIES_FILENAME);
        Collection<FeedCategory> categories = null;
        if (file.exists()) {
            try {
                categories = mapper.readValue(file, new TypeReference<List<FeedCategory>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return categories;
    }

    public Collection<FeedMetadata> getFeedsFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath + "/" + FEED_METADATA_FILENAME);
        Collection<FeedMetadata> feeds = null;
        if (file.exists()) {
            try {
                feeds = mapper.readValue(file, new TypeReference<List<FeedMetadata>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return feeds;
    }

    public Collection<RegisteredTemplate> getTemplatesFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath + "/" + TEMPLATE_METADATA_FILENAME);
        Collection<RegisteredTemplate> templates = null;
        if (file.exists()) {
            try {
                templates = mapper.readValue(file, new TypeReference<List<RegisteredTemplate>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return templates;
    }

    private static class LazyHolder {

        static final FileObjectPersistence INSTANCE = new FileObjectPersistence();
    }
}
