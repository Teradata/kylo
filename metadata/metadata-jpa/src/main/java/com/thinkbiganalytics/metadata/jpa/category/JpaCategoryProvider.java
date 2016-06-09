package com.thinkbiganalytics.metadata.jpa.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.jpa.BaseJpaProvider;
import com.thinkbiganalytics.metadata.jpa.NamedJpaQueries;

import javax.persistence.NoResultException;
import java.io.Serializable;

/**
 * Created by sr186054 on 5/3/16.
 */
public class JpaCategoryProvider extends BaseJpaProvider<Category,Category.ID> implements CategoryProvider<Category> {

    @Override
    public Class<? extends Category> getEntityClass() {
        return JpaCategory.class;
    }
    @Override
    public Category findBySystemName(String systemName) {

        Category category =  null;
        try {
            category = (Category) entityManager.createNamedQuery(NamedJpaQueries.CATEGORY_FIND_BY_SYSTEM_NAME)
                    .setParameter("systemName", systemName)
                    .getSingleResult();
        }catch(NoResultException e){
            e.printStackTrace();
        }
        return  category;
    }

    @Override
    public Category ensureCategory(String systemName) {
      Category c = findBySystemName(systemName);
        if(c == null){
            JpaCategory cat = new JpaCategory();
            cat.setName(systemName);
            c = create(cat);
        }
        return  c;

    }

    @Override
    public Category.ID resolveId(Serializable fid) {
        return new JpaCategory.CategoryId(fid);
    }
}
