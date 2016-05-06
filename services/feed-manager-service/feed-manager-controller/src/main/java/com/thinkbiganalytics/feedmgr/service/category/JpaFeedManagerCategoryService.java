package com.thinkbiganalytics.feedmgr.service.category;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public class JpaFeedManagerCategoryService implements FeedManagerCategoryService {

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Override
    public Collection<FeedCategory> getCategories() {
       List<FeedManagerCategory> domainCategories = categoryProvider.findAll();
       return CategoryModelTransform.domainToFeedCategory(domainCategories);
    }

    @Override
    public FeedCategory getCategoryByName(String name) {
       FeedManagerCategory domainCategory = categoryProvider.findBySystemName(name);
        return CategoryModelTransform.DOMAIN_TO_FEED_CATEGORY.apply(domainCategory);
    }

    @Override
    public FeedCategory getCategoryById(String id) {
        FeedManagerCategory.ID domainId = categoryProvider.resolveId(id);
        FeedManagerCategory domainCategory = categoryProvider.findById(domainId);
        return CategoryModelTransform.DOMAIN_TO_FEED_CATEGORY.apply(domainCategory);
    }

    @Override
    public FeedCategory getCategoryBySystemName(String name) {
        FeedManagerCategory domainCategory = categoryProvider.findBySystemName(name);
        return CategoryModelTransform.DOMAIN_TO_FEED_CATEGORY.apply(domainCategory);
    }

    @Override
    @Transactional(transactionManager = "metadataTransactionManager")
    public void saveCategory(FeedCategory category) {

        if (category.getId() == null) {
            category.generateSystemName();
        } else {
            FeedCategory oldCategory = getCategoryById(category.getId());

            if (oldCategory != null && !oldCategory.getName().equalsIgnoreCase(category.getName())) {
                ///names have changed
                //only regenerate the system name if there are no related feeds
                if (oldCategory.getRelatedFeeds() == 0) {
                    category.generateSystemName();
                }
            }
        }
        FeedManagerCategory domainCategory = CategoryModelTransform.FEED_CATEGORY_TO_DOMAIN.apply(category);
       categoryProvider.update(domainCategory);

    }

    @Override
    @Transactional(transactionManager = "metadataTransactionManager")
    public boolean deleteCategory(String categoryId) throws InvalidOperationException {
        FeedManagerCategory.ID domainId = categoryProvider.resolveId(categoryId);
        categoryProvider.deleteById(domainId);
        return true;
    }
}
