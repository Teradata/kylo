package com.thinkbiganalytics.feedmgr.service.category;

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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategoryBuilder;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.service.FileObjectPersistence;
import com.thinkbiganalytics.security.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

/**
 * An in-memory implementation of {@link FeedManagerCategoryService} for testing the REST API.
 */
public class InMemoryFeedManagerCategoryService implements FeedManagerCategoryService {

    private Map<String, FeedCategory> categories = new HashMap<>();

    @PostConstruct
    private void loadCategories() {
        Collection<FeedCategory> savedCategories = FileObjectPersistence.getInstance().getCategoriesFromFile();
        if (savedCategories != null) {
            for (FeedCategory c : savedCategories) {
                categories.put(c.getId(), c);
            }
        }
        if (categories.isEmpty()) {
            bootstrapCategories();
        }

    }

    private void bootstrapCategories() {

        List<FeedCategory> feedCategoryList = new ArrayList<>();
        feedCategoryList.add(
            new FeedCategoryBuilder("Employees").description("Employee profile data and records").icon("people").iconColor("#F06292")
                .build());
        feedCategoryList.add(
            new FeedCategoryBuilder("Sales").description("Sales data including opportunities and leads").icon("phone_android")
                .iconColor("#90A4AE").build());
        feedCategoryList.add(
            new FeedCategoryBuilder("Online").description("Web traffic data and reports of online activity").icon("web")
                .iconColor("#66BB6A").build());
        feedCategoryList.add(
            new FeedCategoryBuilder("Payroll").description("Payroll records for employees").icon("attach_money").iconColor("#FFCA28")
                .build());
        feedCategoryList.add(new FeedCategoryBuilder("Travel").description("Employee travel records including all expense reports")
                                 .icon("local_airport").iconColor("#FFF176").build());
        feedCategoryList.add(new FeedCategoryBuilder("Data").description("General Data ").icon("cloud").iconColor("#AB47BC").build());
        feedCategoryList.add(
            new FeedCategoryBuilder("Emails").description("All email traffic data archived for the last 5 years").icon("email")
                .iconColor("#FF5252").build());
        feedCategoryList.add(new FeedCategoryBuilder("Customers").description("All customer data for various companies").icon("face")
                                 .iconColor("#FF5252").build());

        for (FeedCategory category : feedCategoryList) {
            category.setId(UUID.randomUUID().toString());
            categories.put(category.getId(), category);
        }

    }

    @Override
    public boolean checkCategoryPermission(String id, Action action, Action... more) {
        return true;
    }

    @Override
    public Collection<FeedCategory> getCategories() {
        return categories.values();
    }

    @Override
    public Collection<FeedCategory> getCategories(boolean includeFeedDetails) {
        return categories.values();
    }

    @Override
    public FeedCategory getCategoryBySystemName(final String name) {
        return Iterables.tryFind(categories.values(), new Predicate<FeedCategory>() {
            @Override
            public boolean apply(FeedCategory feedCategory) {
                return feedCategory.getSystemName().equalsIgnoreCase(name);
            }
        }).orNull();
    }

    @Override
    public FeedCategory getCategoryById(final String id) {
        return Iterables.tryFind(categories.values(), new Predicate<FeedCategory>() {
            @Override
            public boolean apply(FeedCategory feedCategory) {
                return feedCategory.getId().equalsIgnoreCase(id);
            }
        }).orNull();
    }

    @Override
    public void saveCategory(final FeedCategory category) {
        if (category.getId() == null) {
            category.setId(UUID.randomUUID().toString());
            category.setSystemName(SystemNamingService.generateSystemName(category.getName()));
        } else {
            FeedCategory oldCategory = categories.get(category.getId());

            if (oldCategory != null && !oldCategory.getName().equalsIgnoreCase(category.getName())) {
                ///names have changed
                //only regenerate the system name if there are no related feeds
                if (oldCategory.getRelatedFeeds() == 0) {
                    category.setSystemName(SystemNamingService.generateSystemName(category.getName()));
                }
            }
            List<FeedSummary> feeds = categories.get(category.getId()).getFeeds();

            category.setFeeds(feeds);

        }
        categories.put(category.getId(), category);

        FileObjectPersistence.getInstance().writeCategoriesToFile(categories.values());
    }

    @Override
    public boolean deleteCategory(String categoryId) throws InvalidOperationException {
        FeedCategory category = categories.get(categoryId);
        if (category != null) {
            //dont allow if category has feeds on it
            if (category.getRelatedFeeds() > 0) {
                throw new InvalidOperationException(
                    "Unable to delete Category " + category.getName() + ".  This category has " + category.getRelatedFeeds()
                    + " feeds associated to it.");
            } else {
                categories.remove(categoryId);
                FileObjectPersistence.getInstance().writeCategoriesToFile(categories.values());
                return true;
            }
        }
        return false;

    }

    @Nonnull
    @Override
    public Set<UserField> getUserFields() {
        return Collections.emptySet();
    }

    @Override
    public void setUserFields(@Nonnull final Set<UserField> userFields) {
        // do nothing
    }

    @Nonnull
    @Override
    public Set<UserProperty> getUserProperties() {
        return Collections.emptySet();
    }
}
