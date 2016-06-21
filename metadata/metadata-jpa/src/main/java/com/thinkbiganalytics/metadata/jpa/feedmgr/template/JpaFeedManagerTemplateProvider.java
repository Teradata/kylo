package com.thinkbiganalytics.metadata.jpa.feedmgr.template;

import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.jpa.BaseJpaProvider;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;

import javax.persistence.NoResultException;
import java.io.Serializable;

/**
 * Created by sr186054 on 5/3/16.
 */
public class JpaFeedManagerTemplateProvider  extends BaseJpaProvider<FeedManagerTemplate,FeedManagerTemplate.ID> implements FeedManagerTemplateProvider {
    @Override

    public Class<? extends FeedManagerTemplate> getEntityClass() {
        return JpaFeedManagerTemplate.class;
    }

    public FeedManagerTemplate findByName(String name){

        JpaFeedManagerTemplate template =  null;
        try {
            template = (JpaFeedManagerTemplate) entityManager.createNamedQuery(FeedManagerNamedQueries.TEMPLATE_FIND_BY_NAME)
                    .setParameter("name", name)
                    .getSingleResult();
        }catch(NoResultException e){
           // e.printStackTrace();
        }
        return template;
    }

    public FeedManagerTemplate findByNifiTemplateId(String nifiTemplateId){

        JpaFeedManagerTemplate template =  null;
        try {
            template = (JpaFeedManagerTemplate) entityManager.createNamedQuery(FeedManagerNamedQueries.TEMPLATE_FIND_BY_NIFI_ID)
                    .setParameter("nifiTemplateId", nifiTemplateId)
                    .getSingleResult();
        }catch(NoResultException e){
            // e.printStackTrace();
        }
        return template;
    }

    public FeedManagerTemplate ensureTemplate(String name) {
        FeedManagerTemplate template = findByName(name);
        if (template == null) {
            template = new JpaFeedManagerTemplate(name);
        }
        return template;
    }

    @Override
    public FeedManagerTemplate.ID resolveId(Serializable fid) {
        return new JpaFeedManagerTemplate.FeedManagerTemplateId(fid);
    }
}
