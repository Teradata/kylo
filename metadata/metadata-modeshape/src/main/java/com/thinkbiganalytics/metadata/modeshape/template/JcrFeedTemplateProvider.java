package com.thinkbiganalytics.metadata.modeshape.template;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 6/8/16.
 */
public class JcrFeedTemplateProvider extends BaseJcrProvider<FeedManagerTemplate, FeedManagerTemplate.ID> implements FeedManagerTemplateProvider {


    @Override
    public Class<? extends FeedManagerTemplate> getEntityClass() {
        return JcrFeedTemplate.class;
    }

    @Override
    public Class<? extends JcrEntity> getJcrEntityClass() {
        return JcrFeedTemplate.class;
    }

    @Override
    public String getNodeType() {
        return JcrFeedTemplate.NODE_TYPE;
    }

    public FeedManagerTemplate ensureTemplate(String systemName) {
        String path = EntityUtil.pathForTemplates();
        Map<String, Object> props = new HashMap<>();
        props.put(JcrFeedTemplate.TITLE, systemName);
        return findOrCreateEntity(path, systemName, props);
    }

    @Override
    public FeedManagerTemplate findByName(String name) {
        String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeedTemplate.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedTemplate.TITLE) + " = title";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("title", name);
        return JcrUtil.findFirst(getSession(), query, JcrFeedTemplate.class);
    }

    @Override
    public FeedManagerTemplate findByNifiTemplateId(String nifiTemplateId) {
        String
            query =
            "SELECT * from " + EntityUtil.asQueryProperty(JcrFeedTemplate.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedTemplate.NIFI_TEMPLATE_ID) + " = $nifiTemplateId";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("nifiTemplateId", nifiTemplateId);
        return JcrUtil.findFirst(getSession(), query, JcrFeedTemplate.class);

    }
}
