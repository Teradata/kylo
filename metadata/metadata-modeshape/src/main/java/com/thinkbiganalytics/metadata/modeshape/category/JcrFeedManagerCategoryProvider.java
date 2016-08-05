package com.thinkbiganalytics.metadata.modeshape.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * A JCR provider for {@link FeedManagerCategory} objects.
 */
public class JcrFeedManagerCategoryProvider extends BaseJcrProvider<FeedManagerCategory, Category.ID> implements FeedManagerCategoryProvider {

    @Inject
    private CategoryProvider categoryProvider;

    @Override
    public Class<? extends FeedManagerCategory> getEntityClass() {
        return JcrFeedManagerCategory.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {

        return JcrFeedManagerCategory.class;
    }

    @Override
    public String getNodeType() {
        return ((JcrCategoryProvider) categoryProvider).getNodeType();
    }

    @Override
    public FeedManagerCategory findBySystemName(String systemName) {
        JcrCategory c = (JcrCategory) categoryProvider.findBySystemName(systemName);
        if (c != null) {
            return new JcrFeedManagerCategory(c);
        }
        return null;
    }

    @Override
    public FeedManagerCategory ensureCategory(String systemName) {
        JcrCategory c = (JcrCategory) categoryProvider.ensureCategory(systemName);
        if (c != null) {
            return new JcrFeedManagerCategory(c);
        }
        return null;
    }

    public Category.ID resolveId(Serializable fid) {
        return new JcrCategory.CategoryId(fid);
    }
}
