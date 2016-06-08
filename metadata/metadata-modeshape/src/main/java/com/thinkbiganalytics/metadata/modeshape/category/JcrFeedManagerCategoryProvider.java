package com.thinkbiganalytics.metadata.modeshape.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

/**
 * Created by sr186054 on 6/8/16.
 */
public class JcrFeedManagerCategoryProvider extends JcrCategoryProvider implements FeedManagerCategoryProvider {

    @Override
    public FeedManagerCategory findBySystemName(String systemName) {
        Category c = super.findBySystemName(systemName);
        return (FeedManagerCategory) c;
    }

    @Override
    public String getNodeType() {
        return super.getNodeType();
    }

    @Override
    public Class<? extends Category> getEntityClass() {
        return JcrFeedManagerCategory.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrFeedManagerCategory.class;
    }

}
