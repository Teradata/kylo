package com.thinkbiganalytics.feedmgr.service.category;
/*-
 * #%L
 * thinkbig-feed-manager-core
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.event.category.CategoryChange;
import com.thinkbiganalytics.metadata.api.event.category.CategoryChangeEvent;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 *
 */
public class SimpleCategoryCache implements ClusterServiceMessageReceiver {

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private SimpleCategoryModelTransform categoryModelTransform;


    private AtomicBoolean populated = new AtomicBoolean(false);

    @Inject
    private MetadataEventService metadataEventService;

    private CategoryChangeListener categoryChangeListener = new CategoryChangeListener();

    /**
     * Adds listeners for transferring events.
     */
    @PostConstruct
    public void addEventListener() {
        metadataEventService.addListener(categoryChangeListener);
    }

    /**
     * Removes listeners and stops transferring events.
     */
    @PreDestroy
    public void removeEventListener() {
        metadataEventService.removeListener(categoryChangeListener);
    }





    private LoadingCache<String, FeedCategory> categoryIdCache = CacheBuilder.newBuilder().build(new CacheLoader<String, FeedCategory>() {
        @Override
        public FeedCategory load(String id) throws Exception {
            FeedCategory feedCategory = metadataAccess.read(() -> {
                                                       Category category = categoryProvider.findById(categoryProvider.resolveId(id));
                                                        if (category != null) {
                                                            FeedCategory c = categoryModelTransform.domainToFeedCategorySimple(category, false, false);
                                                            categoryNameCache.put(c.getSystemName(),c);
                                                            return c;
                                                        } else {
                                                            return null;
                                                        }
                                                    },MetadataAccess.SERVICE);
            return feedCategory;

        }
    });

    private LoadingCache<String, FeedCategory> categoryNameCache = CacheBuilder.newBuilder().build(new CacheLoader<String, FeedCategory>() {
        @Override
        public FeedCategory load(String name) throws Exception {
            FeedCategory feedCategory = metadataAccess.read(() -> {
                Category category = categoryProvider.findBySystemName(name);
                if (category != null) {
                    return categoryModelTransform.domainToFeedCategorySimple(category, false, false);
                } else {
                    return null;
                }
            },MetadataAccess.SERVICE);
            return feedCategory;

        }
    });


    private synchronized void populate(){
        List<FeedCategory> allCategories = metadataAccess.read(() -> {
          List<FeedCategory> list = categoryProvider.findAll().stream().map(c -> categoryModelTransform.domainToFeedCategorySimple(c, false, false)).collect(Collectors.toList());
            return list;
      });
        Map<String,FeedCategory> idMap =   allCategories.stream().collect(Collectors.toMap(c -> c.getId(), c->c));
        Map<String,FeedCategory> nameMap =   allCategories.stream().collect(Collectors.toMap(c -> c.getSystemName(), c->c));
      categoryIdCache.putAll(idMap);
      categoryNameCache.putAll(nameMap);
      populated.set(true);
    }

    public FeedCategory getFeedCategoryById(String id){
        if(!populated.get()) {
            populate();
        }
        return categoryIdCache.getUnchecked(id);
    }

    public FeedCategory getFeedCategoryByName(String name){
        if(!populated.get()) {
            populate();
        }
        return categoryNameCache.getUnchecked(name);
    }

    public Map<String,FeedCategory> getCategoriesByName(){
        if(!populated.get()) {
            populate();
        }
        return categoryNameCache.asMap();
    }

    public Map<String,FeedCategory> getCategoriesById(){
        if(!populated.get()) {
            populate();
        }
        return categoryIdCache.asMap();
    }

    private class CategoryChangeListener implements MetadataEventListener<CategoryChangeEvent> {

        public void notify(@Nonnull final CategoryChangeEvent metadataEvent) {
            CategoryChange change = metadataEvent.getData();
            categoryIdCache.refresh(change.getCategoryId().toString());
        }
    }


    @Override
    public void onMessageReceived(String from, ClusterMessage message) {
        if(CategoryChange.CLUSTER_EVENT_TYPE.equals(message.getType())){
            CategoryChange change = (CategoryChange)message.getMessage();
            if(change != null){
                if(change.getChange() == MetadataChange.ChangeType.DELETE){
                    categoryIdCache.invalidate(change.getCategoryId().toString());
                    if(change.getCategoryName().isPresent()){
                        categoryNameCache.invalidate(change.getCategoryName().get());
                    }
                }
                else {
                    categoryIdCache.refresh(change.getCategoryId().toString());
                }
            }
        }
    }
}
