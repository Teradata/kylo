package com.thinkbiganalytics.metadata.jpa.feedmgr.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.jpa.BaseJpaProvider;
import com.thinkbiganalytics.metadata.jpa.NamedJpaQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/3/16.
 */
public class JpaFeedManagerCategoryProvider extends BaseJpaProvider<FeedManagerCategory,FeedManagerCategory.ID> implements FeedManagerCategoryProvider {

    @Override
    public Class<? extends FeedManagerCategory> getEntityClass() {
        return JpaFeedManagerCategory.class;
    }
    @Override
    public FeedManagerCategory findBySystemName(String systemName) {

        FeedManagerCategory category =  null;
        try {
            category = (FeedManagerCategory) entityManager.createNamedQuery(FeedManagerNamedQueries.CATEGORY_FIND_BY_SYSTEM_NAME)
                    .setParameter("systemName", systemName)
                    .getSingleResult();
        }catch(NoResultException e){
            e.printStackTrace();
        }
        return category;
    }


    @Override
    public Category.ID resolveId(Serializable fid) {
        return new JpaFeedManagerCategory.CategoryId(fid);
    }
}
