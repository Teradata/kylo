package com.thinkbiganalytics.metadata.modeshape.template;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContextHolder;

import com.thinkbiganalytics.metadata.api.event.MetadataChange.ChangeType;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChange;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChangeEvent;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.template.TemplateDeletionException;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.BaseJcrProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.EntityUtil;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedEntityActionsProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

/**
 */
public class JcrFeedTemplateProvider extends BaseJcrProvider<FeedManagerTemplate, FeedManagerTemplate.ID> implements FeedManagerTemplateProvider {

    @Inject
    private MetadataEventService metadataEventService;
    
    @Inject
    private SecurityRoleProvider roleProvider;

    @Inject
    private JcrAllowedEntityActionsProvider actionsProvider;
    
    @Inject
    private AccessController accessController;

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

    @Override
    protected String getEntityQueryStartingPath() {
        return EntityUtil.pathForTemplates();
    }


    public FeedManagerTemplate ensureTemplate(String systemName) {
        String sanitizedName = sanitizeSystemName(systemName);
        String path = EntityUtil.pathForTemplates();
        Map<String, Object> props = new HashMap<>();
        props.put(JcrFeedTemplate.TITLE, sanitizedName);
        boolean newTemplate = !JcrUtil.hasNode(getSession(), path, sanitizedName);
        JcrFeedTemplate template = (JcrFeedTemplate) findOrCreateEntity(path, sanitizedName, props);

        if (newTemplate) {
            if (this.accessController.isEntityAccessControlled()) {
                List<SecurityRole> roles = this.roleProvider.getEntityRoles(SecurityRole.TEMPLATE);
                this.actionsProvider.getAvailableActions(AllowedActions.TEMPLATE)
                    .ifPresent(actions -> template.enableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser(), roles));
            } else {
                this.actionsProvider.getAvailableActions(AllowedActions.TEMPLATE)
                .ifPresent(actions -> template.disableAccessControl((JcrAllowedActions) actions, JcrMetadataAccess.getActiveUser()));
            }
            
            addPostFeedChangeAction(template, ChangeType.CREATE);
        }

        return template;
    }



    @Override
    public FeedManagerTemplate findByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            String sanitizedTitle = sanitizeTitle(name);
            String query = "SELECT * from " + EntityUtil.asQueryProperty(JcrFeedTemplate.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedTemplate.TITLE) + " = $title ";
            query = applyFindAllFilter(query,EntityUtil.pathForTemplates());
            Map<String, String> bindParams = new HashMap<>();
            bindParams.put("title", sanitizedTitle);
            return JcrQueryUtil.findFirst(getSession(), query, bindParams, JcrFeedTemplate.class);
        } else {
            return null;
        }
    }

    @Override
    public FeedManagerTemplate findByNifiTemplateId(String nifiTemplateId) {
        String
            query =
            "SELECT * from " + EntityUtil.asQueryProperty(JcrFeedTemplate.NODE_TYPE) + " as e where e." + EntityUtil.asQueryProperty(JcrFeedTemplate.NIFI_TEMPLATE_ID) + " = $nifiTemplateId ";
        query = applyFindAllFilter(query,EntityUtil.pathForTemplates());
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("nifiTemplateId", nifiTemplateId);
        return JcrQueryUtil.findFirst(getSession(), query, bindParams, JcrFeedTemplate.class);

    }

    public FeedManagerTemplate.ID resolveId(Serializable fid) {
        return new JcrFeedTemplate.FeedTemplateId(fid);
    }


    @Override
    public FeedManagerTemplate enable(FeedManagerTemplate.ID id) {
        JcrFeedTemplate template = (JcrFeedTemplate) findById(id);
        if (template != null) {
            if (!template.isEnabled()) {
                template.enable();
                addPostFeedChangeAction(template, ChangeType.UPDATE);
                return update(template);
            }
            return template;
        } else {
            throw new MetadataRepositoryException("Unable to find template with id" + id);
        }
    }

    @Override
    public FeedManagerTemplate disable(FeedManagerTemplate.ID id) {
        JcrFeedTemplate template = (JcrFeedTemplate) findById(id);
        if (template != null) {
            if (template.isEnabled()) {
                template.disable();
                addPostFeedChangeAction(template, ChangeType.UPDATE);
                return update(template);
            }
            return template;
        } else {
            throw new MetadataRepositoryException("Unable to find template with id" + id);
        }
    }

    @Override
    public boolean deleteTemplate(FeedManagerTemplate.ID id) throws TemplateDeletionException {
        FeedManagerTemplate item = findById(id);
        return deleteTemplate(item);
    }

    public boolean deleteTemplate(FeedManagerTemplate feedManagerTemplate) throws TemplateDeletionException {
        if (feedManagerTemplate != null && (feedManagerTemplate.getFeeds() == null || feedManagerTemplate.getFeeds().size() == 0)) {
            feedManagerTemplate.getAllowedActions().checkPermission(TemplateAccessControl.DELETE);
            addPostFeedChangeAction(feedManagerTemplate, ChangeType.DELETE);
            super.delete(feedManagerTemplate);
            return true;
        } else {
            throw new TemplateDeletionException(feedManagerTemplate.getName(), feedManagerTemplate.getId().toString(), "There are still feeds assigned to this template.");
        }
    }

    @Override
    public void delete(FeedManagerTemplate feedManagerTemplate) {
        deleteTemplate(feedManagerTemplate);
    }

    @Override
    public void deleteById(FeedManagerTemplate.ID id) {
        deleteTemplate(id);
    }

    /**
     * Registers an action that produces a template change event upon a successful transaction commit.
     *
     * @param template the feed to being created
     */
    private void addPostFeedChangeAction(FeedManagerTemplate template, ChangeType changeType) {
        FeedManagerTemplate.State state = template.getState();
        FeedManagerTemplate.ID id = template.getId();
        String desc = template.getName();
        DateTime createTime = template.getCreatedTime();
        final Principal principal = SecurityContextHolder.getContext().getAuthentication() != null
                                    ? SecurityContextHolder.getContext().getAuthentication()
                                    : null;

        Consumer<Boolean> action = (success) -> {
            if (success) {
                TemplateChange change = new TemplateChange(changeType, desc, id, state);
                TemplateChangeEvent event = new TemplateChangeEvent(change, createTime, principal);
                metadataEventService.notify(event);
            }
        };

        JcrMetadataAccess.addPostTransactionAction(action);
    }

}
