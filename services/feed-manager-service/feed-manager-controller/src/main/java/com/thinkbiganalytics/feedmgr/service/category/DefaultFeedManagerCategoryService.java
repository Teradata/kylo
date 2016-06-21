package com.thinkbiganalytics.feedmgr.service.category;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/4/16.
 */
public class DefaultFeedManagerCategoryService implements FeedManagerCategoryService {

    @Inject
    FeedManagerCategoryProvider categoryProvider;

    @Inject
    CategoryModelTransform categoryModelTransform;

    @Inject
    MetadataAccess metadataAccess;

    @Override
    public Collection<FeedCategory> getCategories() {
        return metadataAccess.read(new Command<Collection<FeedCategory>>() {
            @Override
            public Collection<FeedCategory> execute() {
                List<FeedManagerCategory> domainCategories = categoryProvider.findAll();
                return categoryModelTransform.domainToFeedCategory(domainCategories);
            }
        });

    }


    @Override
    public FeedCategory getCategoryById(final String id) {
        return metadataAccess.read(new Command<FeedCategory>() {
            @Override
            public FeedCategory execute() {
                FeedManagerCategory.ID domainId = categoryProvider.resolveId(id);
                FeedManagerCategory domainCategory = categoryProvider.findById(domainId);
                return categoryModelTransform.DOMAIN_TO_FEED_CATEGORY.apply(domainCategory);
            }
        });


    }

    @Override
    public FeedCategory getCategoryBySystemName(final String name) {
        return metadataAccess.read(new Command<FeedCategory>() {
            @Override
            public FeedCategory execute() {
                FeedManagerCategory domainCategory = categoryProvider.findBySystemName(name);
                return categoryModelTransform.DOMAIN_TO_FEED_CATEGORY.apply(domainCategory);
            }
        });
    }

    @Override
  //  @Transactional(transactionManager = "metadataTransactionManager")
    public void saveCategory(final FeedCategory category) {
        metadataAccess.commit(new Command<FeedCategory>() {
            @Override
            public FeedCategory execute() {
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
                FeedManagerCategory domainCategory = categoryModelTransform.FEED_CATEGORY_TO_DOMAIN.apply(category);
                categoryProvider.update(domainCategory);
                return category;
            }

        });



    }

    @Override
   // @Transactional(transactionManager = "metadataTransactionManager")
    public boolean deleteCategory(final String categoryId) throws InvalidOperationException {
        return metadataAccess.commit(new Command<Boolean>() {
            @Override
            public Boolean execute() {
                FeedManagerCategory.ID domainId = categoryProvider.resolveId(categoryId);
                categoryProvider.deleteById(domainId);
                return true;
            }
        });

    }
}
