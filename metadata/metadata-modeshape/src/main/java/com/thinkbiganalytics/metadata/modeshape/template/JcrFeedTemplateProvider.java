package com.thinkbiganalytics.metadata.modeshape.template;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;

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
    public String getNodeType(Class<? extends JcrEntity> jcrEntityType) {
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
        if (StringUtils.isNotBlank(name)) {
            String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeedTemplate.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedTemplate.TITLE) + " = $title";
            Map<String, String> bindParams = new HashMap<>();
            bindParams.put("title", name);
            return JcrQueryUtil.findFirst(getSession(), query, bindParams, JcrFeedTemplate.class);
        } else {
            return null;
        }
    }

    @Override
    public FeedManagerTemplate findByNifiTemplateId(String nifiTemplateId) {
        String
            query =
            "SELECT * from " + EntityUtil.asQueryProperty(JcrFeedTemplate.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedTemplate.NIFI_TEMPLATE_ID) + " = $nifiTemplateId";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("nifiTemplateId", nifiTemplateId);
        return JcrQueryUtil.findFirst(getSession(), query, bindParams, JcrFeedTemplate.class);

    }

    public FeedManagerTemplate.ID resolveId(Serializable fid) {
        return new JcrFeedTemplate.FeedTemplateId(fid);
    }
}
