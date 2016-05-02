package com.thinkbiganalytics.feedmgr.service;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;

import java.util.Collection;

/**
 * Created by sr186054 on 5/1/16.
 */
public interface FeedManagerCategoryProvider {

  Collection<FeedCategory> getCategories();

  FeedCategory getCategoryByName(String name);

  FeedCategory getCategoryById(String id);

  FeedCategory getCategoryBySystemName(String name);

  void saveCategory(FeedCategory category);

  boolean deleteCategory(String categoryId) throws InvalidOperationException;
}
